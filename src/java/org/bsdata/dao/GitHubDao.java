
package org.bsdata.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.viewmodel.RepositoryVm;
import org.bsdata.viewmodel.RepositoryFileVm;
import org.bsdata.viewmodel.RepositoryListVm;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.ApplicationProperties;
import org.bsdata.utils.Utils;
import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;


/**
 * Handles all reading and writing of data files to/from GitHub.
 * Methods in this class will generally convert GitHub objects to/from simpler objects from the viewmodel package.
 * These viewmodel objects can then be converted to/from JSON for sending/receiving over the wire.
 * 
 * @author Jonskichov
 */
public class GitHubDao {
    
    private static final SimpleDateFormat branchDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
    private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    private static HashMap<String, Integer> repoNumReleasesCache;
    private static HashMap<String, HashMap<String, byte[]>> repoFileCache;
    private static HashMap<String, ReentrantLock> repoDownloadLocks;
    
    private Indexer indexer;
    
    public GitHubDao() {
        indexer = new Indexer();
    }
    
    /**
     * Gets a connection to GitHub for BSDataAnon using the OAuth token in bsdata.properties
     * 
     * @return
     * @throws IOException 
     */
    private GitHubClient connectToGitHub() throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(properties.getProperty(PropertiesConstants.GITHUB_TOKEN));
        gitHubClient.setUserAgent(properties.getProperty(PropertiesConstants.GITHUB_USERNAME));
        return gitHubClient;
    }
    
    private Repository getBsDataRepository(GitHubClient gitHubClient, String repositoryName) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        return repositoryService.getRepository(
                properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION), 
                repositoryName);
    }
    
    private List<RepositoryContents> getRepositoryContents(Repository repository, Release release) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        ContentsService contentsService = new ContentsService(gitHubClient);
        
        if (release == null) {
            // We didn't find a release for this repo. Just return the HEAD contents.
            return contentsService.getContents(repository);
        }
        else {
            return contentsService.getContents(repository, null, release.getTagName());
        }
    }
    
    /**
     * Get BSDataAnon's fork of the main repo.
     * 1) Search for the fork.
     * 2) If we can't find it, fork the main repo and return the fork.
     * 3) Otherwise compare the number of releases in the fork to the number of releases in the main repo.
     * 4) If the number of releases is different, delete the fork, re-fork the main repo and return the fork.
     * 5) Otherwise just return the fork we found in the search.
     * 
     * @param gitHubClient
     * @param repositoryName
     * @return
     * @throws IOException 
     */
    private Repository getRepositoryFork(GitHubClient gitHubClient, Repository masterRepository) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        
        List<SearchRepository> searchRepositories = repositoryService.searchRepositories(
                masterRepository.getName()
                + " user:" + properties.getProperty(PropertiesConstants.GITHUB_USERNAME) 
                + " fork:only");
        
        if (searchRepositories.isEmpty()) {
            return repositoryService.forkRepository(masterRepository);
        }
        
        Repository repositoryFork = repositoryService.getRepository(
                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
                masterRepository.getName());
        
        if (repositoryService.getTags(repositoryFork).size() == repositoryService.getTags(masterRepository).size()) {
            return repositoryFork;
        }
        else {
            gitHubClient.delete("/repos/" + properties.getProperty(PropertiesConstants.GITHUB_USERNAME) + "/" + repositoryFork.getName());
            return repositoryService.forkRepository(masterRepository);
        }
    }
    
    /**
     * Ensures data files are cached for all data file repositories.
     * 
     * @param baseUrl
     * @throws IOException 
     */
    public synchronized void primeCache(String baseUrl) throws IOException {
        // Clear the cache so it forces data to be re-cached.
        repoFileCache = null;
        repoNumReleasesCache = null;
        RepositoryListVm repositoryList = getRepos(baseUrl);
        for (RepositoryVm repository : repositoryList.getRepositories()) {
            getRepoFileData(repository.getName(), baseUrl, null);
        }
    }
    
    /**
     * Gets the data files for a particular data file repository. File data is cached for 24hrs.
     * 
     * @param repositoryName
     * @param baseUrl
     * @param repositoryUrls
     * @return
     * @throws IOException 
     */
    public HashMap<String, byte[]> getRepoFileData(
            String repositoryName, 
            String baseUrl, 
            List<String> repositoryUrls) throws IOException {
        
        if (repoFileCache == null) {
            repoFileCache = new HashMap<>();
        }
        if (repoNumReleasesCache == null) {
            repoNumReleasesCache = new HashMap<>();
        }
        if (repoDownloadLocks == null) {
            repoDownloadLocks = new HashMap<>();
        }
        
        if (!repoDownloadLocks.containsKey(repositoryName)) {
            // Create a lock object for this repo name if we don't already have one
            repoDownloadLocks.put(repositoryName, new ReentrantLock(true));
        }
        
        // Get the lock object associated with this repo (want to prevent multiple threads downloading from the same repo at the same time)
        ReentrantLock downloadLock = repoDownloadLocks.get(repositoryName);
        if (downloadLock.isLocked() && repoFileCache.containsKey(repositoryName)) {
            // We are currently downloading for this repo, but we already have something cached, so return that
            return repoFileCache.get(repositoryName);
        }

        GitHubClient gitHubClient = connectToGitHub();
        ReleaseService releaseService = new ReleaseService(gitHubClient);

        Repository masterRepository = getBsDataRepository(gitHubClient, repositoryName);
        List<Release> releases = releaseService.getReleases(masterRepository);
        if (requiresDownload(repositoryName, releases)) {
            try {
                // Lock as we don't want multiple threads downloading from the same repo at once
                downloadLock.lock();
                
                // Check again if we need to do a download.
                // We may have a case where one thread has locked this code and is doing the download, and another thread is waiting behind the lock (i.e. requiresDownload returned true for the second thread).
                // This can happen if we are downloading the data for the first time (i.e. repoFileCache is empty while the first thread does the download).
                // In this case, the waiting thread should not download the data _again_ and should instead return the data from the cache once it's 
                //   done waiting for the first thread to finish the download.
                if (!requiresDownload(repositoryName, releases)) {
                    repoFileCache.get(repositoryName);
                }

                Release latestRelease = releaseService.getLatestRelease(releases);
                HashMap<String, byte[]> repositoryData = downloadFromGitHub(masterRepository, latestRelease);
                repositoryData = indexer.createRepositoryData(repositoryName, baseUrl, null, repositoryData);

                // Cache the results and the current number of releases
                repoFileCache.put(repositoryName, repositoryData);
                repoNumReleasesCache.put(repositoryName, releases.size());
            }
            finally {
                // Done! Unlock in a finally block as we don't want to leave anything locked if there's an exception...
                downloadLock.unlock();
            }
        }

        return repoFileCache.get(repositoryName);
    }
    
    /**
     * Download the data for the latest release if:
     * 1) We don't already have data cached for this repo
     * 2) Or the repo has a different number of releases since the last time we downloaded the data (i.e. there has been a new release)
     * 
     * @param repositoryName
     * @param releases
     * @return 
     */
    private boolean requiresDownload(String repositoryName, List<Release> releases) {
        if (!repoFileCache.containsKey(repositoryName) 
                || !repoNumReleasesCache.containsKey(repositoryName)
                || repoNumReleasesCache.get(repositoryName).intValue() != releases.size()) {
            return true;
        }
        return false;
    }
    
    /**
     * Downloads all the data files from a specific release in a specific repository.
     * 
     * @param repositoryName
     * @return
     * @throws IOException 
     */
    private HashMap<String, byte[]> downloadFromGitHub(Repository repository, Release release) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        DataService dataService = new DataService(gitHubClient);
        
        List<RepositoryContents> contents = getRepositoryContents(repository, release);
        HashMap<String, byte[]> repoFiles = new HashMap<>();
        for (RepositoryContents repositoryContents : contents) {
            if (!Utils.isDataFilePath(repositoryContents.getName())) {
                continue; // Skip non-data files
            }
            String content = dataService.getBlob(repository, repositoryContents.getSha()).getContent();
            byte[] data = Base64.decodeBase64(content);
            repoFiles.put(repositoryContents.getName(), data);
        }
        
        return repoFiles;
    }
    
    /**
     * Gets details of the data file repositories managed by the system.
     * 
     * @param baseUrl
     * @return
     * @throws IOException 
     */
    public RepositoryListVm getRepos(String baseUrl) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        GitHubClient gitHubClient = connectToGitHub();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        ReleaseService releaseService = new ReleaseService(gitHubClient);
        
        List<Repository> orgRepositories = repositoryService.getOrgRepositories(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION));
        List<RepositoryVm> repositories = new ArrayList<>();
        for (Repository repository : orgRepositories) {
            if (repository.getName().equals(DataConstants.GITHUB_BSDATA_REPO_NAME)) {
                continue;
            }
            
            Release latestRelease = releaseService.getLatestRelease(repository);
            RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl, latestRelease);
            repositories.add(repositoryVm);
        }
        
        // TODO: this should probably be cached...
        RepositoryListVm repositoryList = new RepositoryListVm();
        repositoryList.setRepositories(repositories);
        return repositoryList;
    }
    
    /**
     * Gets details of a particular data file repository and the files stored in it.
     * 
     * @param repositoryName
     * @param baseUrl
     * @return
     * @throws IOException 
     */
    public RepositoryVm getRepoFiles(String repositoryName, String baseUrl) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        ReleaseService releaseService = new ReleaseService(gitHubClient);
        
        Repository repository = getBsDataRepository(gitHubClient, repositoryName);
        Release latestRelease = releaseService.getLatestRelease(repository);
        RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl, latestRelease);
        
        List<RepositoryContents> contents = getRepositoryContents(repository, latestRelease);
        List<RepositoryFileVm> repositoryFiles = new ArrayList<>();
        for (RepositoryContents repositoryContents : contents) {
            String fileName = Utils.getCompressedFileName(repositoryContents.getName());
            if (!Utils.isDataFilePath(fileName)) {
                continue;
            }
            
            RepositoryFileVm repositoryFile = new RepositoryFileVm();
            repositoryFile.setName(fileName);
            repositoryFile.setGitHubUrl(Utils.checkUrl(repositoryVm.getGitHubUrl() + "/blob/master/" + repositoryContents.getPath()));
            repositoryFile.setDataFileUrl(Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + fileName));
            repositoryFiles.add(repositoryFile);
        }
        
        // TODO: this should probably be cached...
        repositoryVm.setRepositoryFiles(repositoryFiles);
        return repositoryVm;
    }
    
    private RepositoryVm createRepositoryVm(Repository repository, String baseUrl, Release latestRelease) {
        RepositoryVm repositoryVm = new RepositoryVm();
        repositoryVm.setName(repository.getName());
        repositoryVm.setDescription(repository.getDescription());
        if (latestRelease != null) {
            repositoryVm.setLastUpdated(longDateFormat.format(latestRelease.getPublishedAt()));
        }
        repositoryVm.setRepoUrl(
                Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME));
        repositoryVm.setGitHubUrl(repository.getHtmlUrl());
        repositoryVm.setBugTrackerUrl(repository.getHtmlUrl() + "/issues");
        return repositoryVm;
    }
    
    /**
     * See http://developer.github.com/v3/git/
     * 
     * 1) Create a fork of the main repository if BSDataAnon doesn't already have one
     * 2) Commit the file submission to a new branch of the fork
     * 3) Create a pull request from the new branch back to the original bsdata repo
     * 
     * @param repositoryName
     * @param fileName
     * @param fileData
     * @throws IOException 
     */
    public void submitFile(String repositoryName, String fileName, byte[] fileData, String commitMessage) throws IOException {
        fileName = FilenameUtils.getName(fileName); // Ensure we have just the filename
        if (Utils.isCompressedPath(fileName)) {
            // If compressed, decompress the data and change the fileName to the uncompressed extension
            fileName = Utils.getUncompressedFileName(fileName);
            fileData = Utils.decompressData(fileData);
        }
        
        GitHubClient gitHubClient = connectToGitHub();
        DataService dataService = new DataService(gitHubClient);
        CommitService commitService = new CommitService(gitHubClient);
        PullRequestService pullRequestService = new PullRequestService(gitHubClient);
        
        // Get BSDataAnon's fork of the repo (creates one if it doesn't already exist)
        Repository repositoryMaster = getBsDataRepository(gitHubClient, repositoryName);
        Repository repositoryFork = getRepositoryFork(gitHubClient, repositoryMaster);
        
        // get the current commit on the master branch in the fork and get the tree it points to
        Reference masterRefFork = dataService.getReference(repositoryFork, "heads/master");
        RepositoryCommit latestMasterCommit = commitService.getCommit(repositoryFork, masterRefFork.getObject().getSha());
        Tree masterTreeFork = latestMasterCommit.getCommit().getTree();
        
        // post a new blob object with new content, getting a blob SHA back
        Blob contentBlobFork = new Blob();
        contentBlobFork.setContent(Base64.encodeBase64String(fileData));
        contentBlobFork.setEncoding("base64");
        String blobSha = dataService.createBlob(repositoryFork, contentBlobFork);
        
        // post a new tree object with file path pointer = your new blob SHA getting a tree SHA back
        TreeEntry treeEntryFork = new TreeEntry();
        treeEntryFork.setPath(fileName);
        treeEntryFork.setMode("100644");
        treeEntryFork.setType("blob");
        treeEntryFork.setSha(blobSha);
        Collection<TreeEntry> treeEntries = new ArrayList<>();
        treeEntries.add(treeEntryFork);
        Tree treeFork = dataService.createTree(repositoryFork, treeEntries, masterTreeFork.getSha());
        
        // create a new commit object with the current commit SHA as the parent and the new tree SHA, getting a commit SHA back
        Commit commitFork = new Commit();
        commitFork.setMessage(commitMessage);
        commitFork.setTree(treeFork);
        List<Commit> commitParentsFork = new ArrayList<>();
        Commit parentCommit = latestMasterCommit.getCommit();
        parentCommit.setSha(latestMasterCommit.getSha()); // For some reason the parentCommit's sha isn't set
        commitParentsFork.add(parentCommit);
        commitFork.setParents(commitParentsFork);
        commitFork = dataService.createCommit(repositoryFork, commitFork);
        
        // create a new branch reference pointing to the new commit SHA
        Reference branchRefFork = new Reference();
        TypedResource resourceFork = new TypedResource();
        resourceFork.setType(TypedResource.TYPE_COMMIT);
        resourceFork.setSha(commitFork.getSha());
        resourceFork.setUrl(commitFork.getUrl());
        branchRefFork.setObject(resourceFork);
        String branchNameFork = FilenameUtils.getBaseName(fileName).trim().replace(" ", "_").replaceAll("[^A-Za-z0-9]", "") 
                + "_" + branchDateFormat.format(new Date());
        branchRefFork.setRef("refs/heads/" + branchNameFork);
        dataService.createReference(repositoryFork, branchRefFork);
        
        // Submit a pull request back to the source repository
        PullRequestMarker sourceRequestMarker = new PullRequestMarker();
        sourceRequestMarker.setLabel("BSDataAnon:" + branchNameFork); // Source is our new branch in the fork
        PullRequestMarker destinationRequestMarker = new PullRequestMarker();
        destinationRequestMarker.setLabel("BSData:master"); // Destination is the master branch of the bsdata repository
        PullRequest pullRequest = new PullRequest();
        pullRequest.setTitle("Anonymous update to " + fileName);
        pullRequest.setHead(sourceRequestMarker);
        pullRequest.setBase(destinationRequestMarker);
        pullRequest.setBody(commitMessage);
        pullRequestService.createPullRequest(repositoryMaster, pullRequest);
    }
    
    public void createIssue(String repositoryName, String title, String body) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        IssueService issueService = new IssueService(gitHubClient);
        
        Repository repository = getBsDataRepository(gitHubClient, repositoryName);
        
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setBody(body);
        
        issueService.createIssue(repository, issue);
    }
}

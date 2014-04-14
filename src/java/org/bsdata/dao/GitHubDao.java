
package org.bsdata.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.constants.WebConstants;
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
  
    private static final Logger logger = Logger.getLogger("org.bsdata");
    
    private static final SimpleDateFormat branchDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
    private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    private static final long REPO_LIST_CACHE_EXPIRY_TIME_MINS = 1;
    private static final long RELEASE_CACHE_EXPIRY_TIME_MINS = 10;
    
    private static Cache<String, List<Repository>> repoListCache;
    private static Cache<String, List<Release>> repoReleasesCache;
    private static Cache<String, HashMap<String, byte[]>> repoFileCache;
    
    private static HashMap<String, ReentrantLock> repoDownloadLocks = new HashMap<>();
    private static HashMap<String, Date> repoReleaseDates = new HashMap<>();
    
    private Indexer indexer;
    
    public GitHubDao() {
        indexer = new Indexer();
        setupCaches();
    }
    
    /**
     * Create the caches if we need to
     */
    private static synchronized void setupCaches() {
        if (repoListCache != null && repoReleasesCache != null && repoFileCache != null) {
            // Caches already created
            return;
        }
        
        repoListCache = CacheBuilder.newBuilder()
            .expireAfterWrite(REPO_LIST_CACHE_EXPIRY_TIME_MINS, TimeUnit.MINUTES)
            .build();
        
        repoReleasesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(RELEASE_CACHE_EXPIRY_TIME_MINS, TimeUnit.MINUTES)
            .build();
        
        repoFileCache = CacheBuilder.newBuilder().build();
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
    
    private List<Repository> getRepositories(final String organizationName) throws IOException {
        try {
            return repoListCache.get(organizationName, new Callable<List<Repository>>() {

                @Override
                public List<Repository> call() throws IOException {
                    logger.log(Level.INFO, "Getting and caching list of repositories for {0}.", organizationName);
                    
                    RepositoryService repositoryService = new RepositoryService(connectToGitHub());
                    List<Repository> repositories = repositoryService.getOrgRepositories(organizationName);
                    if (repositories == null) {
                        // Callable must return a value or throw an exception - can't return null
                        throw new IllegalArgumentException();
                    }
                    return repositories;
                }
            });
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }
            // Callable should only throw an IOException. If we get here, something fishy is going on...
            throw new IllegalStateException(e.getCause());
        }
    }
    
    private Repository getRepository(String organizationName, String repositoryName) throws IOException {
        for (Repository repository : getRepositories(organizationName)) {
            if (repository.getName().equals(repositoryName)) {
                return repository;
            }
        }
        
        // We shouldn't get here
        throw new IllegalArgumentException("Could not find repository " + repositoryName + " in organization " + organizationName);
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
     * Ensures data files are cached for all data file repositories.
     * 
     * @param baseUrl
     * @throws IOException 
     */
    public synchronized void primeCache(String baseUrl) throws IOException {
        // Clear the caches so it forces data to be re-cached.
        repoListCache.invalidateAll();
        repoListCache.cleanUp();
        
        repoReleaseDates = new HashMap<>();
        repoReleasesCache.invalidateAll();
        repoReleasesCache.cleanUp();
        
        repoFileCache.invalidateAll();
        repoFileCache.cleanUp();
        
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        for (Repository repository : getRepositories(organizationName)) {
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
        
        if (!repoDownloadLocks.containsKey(repositoryName)) {
            // Create a lock object for this repo name if we don't already have one
            repoDownloadLocks.put(repositoryName, new ReentrantLock(true));
        }
        
        // Get the lock object associated with this repo (want to prevent multiple threads downloading from the same repo at the same time)
        ReentrantLock downloadLock = repoDownloadLocks.get(repositoryName);
        HashMap<String, byte[]> fileData = repoFileCache.getIfPresent(repositoryName);
        
        if (downloadLock.isLocked() && fileData != null) {
            // We are currently downloading for this repo, but we already have something cached, so return that
            return fileData;
        }

        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repositoryName);
        Release latestRelease = getLatestRelease(repository);
        
        if (requiresDownload(repository, latestRelease)) {
            try {
                // Lock as we don't want multiple threads downloading from the same repo at once
                downloadLock.lock();
                
                // Check again if we need to do a download.
                // We may have a case where one thread has locked this code and is doing the download, and another thread is waiting behind the lock (i.e. requiresDownload returned true for the second thread).
                // This can happen if we are downloading the data for the first time (i.e. repoFileCache is empty while the first thread does the download).
                // In this case, the waiting thread should not download the data _again_ and should instead return the data from the cache once it's 
                //   done waiting for the first thread to finish the download.
                if (!requiresDownload(repository, latestRelease)) {
                    fileData = repoFileCache.getIfPresent(repositoryName);
                    if (fileData == null) {
                        // We should have this cached now. If not, something weird has happened
                        throw new IllegalStateException();
                    }
                    return fileData;
                }

                // Download and cache the repository data files
                logger.log(Level.INFO, "Downloading and caching data for repository {0}.", repository.getName());
                HashMap<String, byte[]> repositoryData = downloadFromGitHub(repository, latestRelease);
                repositoryData = indexer.createRepositoryData(repositoryName, baseUrl, null, repositoryData);
                repoFileCache.put(repositoryName, repositoryData);
            }
            finally {
                // Done! Unlock in a finally block as we don't want to leave anything locked if there's an exception...
                downloadLock.unlock();
            }
        }

        fileData = repoFileCache.getIfPresent(repositoryName);
        if (fileData == null) {
            // We should have this cached now. If not, something weird has happened
            throw new IllegalStateException();
        }
        return fileData;
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
    private boolean requiresDownload(Repository repository, Release latestRelease) {
        HashMap<String, byte[]> fileData = repoFileCache.getIfPresent(repository.getName());
        if (fileData == null) {
            // We have no cached data for this repo.
            logger.log(Level.INFO, "File data for {0} not yet cached.", repository.getName());
            return true;
        }
        
        if (!repoReleaseDates.containsKey(repository.getName())) {
            // We haven't seen a release for this repo yet.
            logger.log(Level.INFO, "Last release date for {0} not yet cached.", repository.getName());
            repoReleaseDates.put(repository.getName(), latestRelease.getPublishedAt());
            return true;
        }
        
        else if (latestRelease.getPublishedAt().after(repoReleaseDates.get(repository.getName()))) {
            // Latest release is newer than the one we last downloaded
            logger.log(Level.INFO, "Latest release date for {0} is after cached last release date.", repository.getName());
            repoReleaseDates.put(repository.getName(), latestRelease.getPublishedAt());
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the first page of releases from GitHub for the given repository
     * 
     * @param repository
     * @return
     * @throws ExecutionException 
     */
    private List<Release> getReleases(final Repository repository) throws IOException {
        try {
            return repoReleasesCache.get(repository.getName(), new Callable<List<Release>>() {
                
                @Override
                public List<Release> call() throws IOException {
                    logger.log(Level.INFO, "Getting and caching releases for {0}.", repository.getName());
                    
                    ReleaseService releaseService = new ReleaseService(connectToGitHub());
                    List<Release> releases = releaseService.getReleases(repository, 1, 1);
                    if (releases == null) {
                        // Callable must return a value or throw an exception - can't return null
                        throw new IllegalArgumentException();
                    }
                    
                    Collections.sort(releases, new Comparator<Release>() {
                        
                        @Override
                        public int compare(Release o1, Release o2) {
                            return o1.getPublishedAt().compareTo(o2.getPublishedAt());
                        }
                    });
                    return releases;
                }
            });
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }
            // Callable should only throw an IOException. If we get here, something fishy is going on...
            throw new IllegalStateException(e.getCause());
        }
    }
    
    private Release getLatestRelease(Repository repository) throws IOException {
        Release latestRelease = null;
        for (Release release : getReleases(repository)) {
            if (latestRelease == null || release.getPublishedAt().after(latestRelease.getPublishedAt())) {
                latestRelease = release;
            }
        }
        return latestRelease;
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
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        List<Repository> orgRepositories = getRepositories(organizationName);
        
        List<RepositoryVm> repositories = new ArrayList<>();
        for (Repository repository : orgRepositories) {
            if (repository.getName().equals(DataConstants.GITHUB_BSDATA_REPO_NAME)) {
                continue;
            }
            
            Release latestRelease = getLatestRelease(repository);
            RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl, latestRelease);
            repositories.add(repositoryVm);
        }
        
        Collections.sort(repositories, new Comparator<RepositoryVm>() {
            @Override
            public int compare(RepositoryVm o1, RepositoryVm o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        
        RepositoryListVm repositoryList = new RepositoryListVm();
        repositoryList.setRepositories(repositories);
        repositoryList.setFeedUrl(baseUrl + "/feeds/all.atom");
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
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repositoryName);
        Release latestRelease = getLatestRelease(repository);
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
        
        Collections.sort(repositoryFiles, new Comparator<RepositoryFileVm>() {
            @Override
            public int compare(RepositoryFileVm o1, RepositoryFileVm o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
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
            repositoryVm.setLastUpdateDescription(latestRelease.getName());
        }
        repositoryVm.setRepoUrl(Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME));
        repositoryVm.setGitHubUrl(repository.getHtmlUrl());
        repositoryVm.setBugTrackerUrl(repository.getHtmlUrl() + "/issues");
        repositoryVm.setFeedUrl(Utils.checkUrl(baseUrl + "/feeds/" + repository.getName() + ".atom"));
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
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repositoryMaster = getRepository(organizationName, repositoryName);
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
    
    public void createIssue(String repositoryName, String title, String body) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        IssueService issueService = new IssueService(gitHubClient);
        
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repositoryName);
        
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setBody(body);
        
        issueService.createIssue(repository, issue);
    }
    
    public Feed getReleaseFeed(String repositoryName, String baseUrl) throws IOException {
        Feed feed = new Feed();
        feed.setFeedType("atom_1.0");
        
        Person author = new Person();
        author.setName("BattleScribe Data");
        author.setUrl(baseUrl.replace("/" + WebConstants.REPO_SERVICE_PATH, ""));
        feed.setAuthors(Collections.singletonList(author));
        
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        List<Entry> entries;
        if (repositoryName.toLowerCase().equals(WebConstants.ALL_REPO_FEEDS)) {
            String feedUrl = Utils.checkUrl(baseUrl + "/feeds/" + WebConstants.ALL_REPO_FEEDS + ".atom");
            
            feed.setId(feedUrl);
            feed.setTitle("All Repository Releases");
            
            Link link = new Link();
            link.setType(DataConstants.HTML_MIME_TYPE);
            link.setHref(getHtmlLink(baseUrl, WebConstants.ALL_REPO_FEEDS));
            feed.setAlternateLinks(Collections.singletonList(link));
            
            Link selfLink = new Link();
            selfLink.setRel("self");
            selfLink.setHref(feedUrl);
            feed.setOtherLinks(Collections.singletonList(selfLink));
            
            Content description = new Content();
            description.setType(DataConstants.TEXT_MIME_TYPE);
            description.setValue("Data file releases for all repositories");
            feed.setSubtitle(description);
            
            entries = new ArrayList<>();
            for (Repository repository : getRepositories(organizationName)) {
                entries.addAll(getReleaseFeedEntries(baseUrl, repository));
            }
        }
        else {
            Repository repository = getRepository(organizationName, repositoryName);
            String feedUrl = Utils.checkUrl(baseUrl + "/feeds/" + repositoryName + ".atom");
            
            feed.setId(feedUrl);
            feed.setTitle(repository.getDescription() + " Releases");
            
            Link link = new Link();
            link.setType(DataConstants.HTML_MIME_TYPE);
            link.setHref(getHtmlLink(baseUrl, repositoryName));
            feed.setAlternateLinks(Collections.singletonList(link));
            
            Link selfLink = new Link();
            selfLink.setRel("self");
            selfLink.setHref(feedUrl);
            feed.setOtherLinks(Collections.singletonList(selfLink));
            
            Content description = new Content();
            description.setType(DataConstants.TEXT_MIME_TYPE);
            description.setValue("Data file releases for " + repository.getDescription());
            feed.setSubtitle(description);
            
            entries = getReleaseFeedEntries(baseUrl, repository);
        }
        
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return o2.getPublished().compareTo(o1.getPublished());
            }
        });
        
        if (!entries.isEmpty()) {
            feed.setUpdated(entries.get(0).getPublished());
        }
        else {
            feed.setUpdated(new Date());
        }
        feed.setEntries(entries);
        return feed;
    }
    
    private List<Entry> getReleaseFeedEntries(String baseUrl, Repository repository) throws IOException {
        List<Entry> entries = new ArrayList<>();
        for (Release release : getReleases(repository)) {
            Entry entry = new Entry();
            
            entry.setId(release.getHtmlUrl());
            entry.setTitle(repository.getDescription() + ": " + release.getName());
            entry.setPublished(release.getPublishedAt());
            entry.setUpdated(release.getPublishedAt());
            
            Link link = new Link();
            link.setType(DataConstants.HTML_MIME_TYPE);
            link.setHref(getHtmlLink(baseUrl, repository.getName()));
            entry.setAlternateLinks(Collections.singletonList(link));
            
            StringBuilder contentBuffer = new StringBuilder();
            if (StringUtils.isEmpty(release.getBody())) {
                contentBuffer.append("<p>(No details)</p>");
            }
            else {
                contentBuffer.append("<p>").append(release.getBody()).append("</p>");
            }
            contentBuffer.append("<p><a href=\"").append(link.getHref()).append("\">Source repository details</a>");
            contentBuffer.append("<br><a href=\"").append(release.getHtmlUrl()).append("\">GitHub release details</a></p>");
            
            Content description = new Content();
            description.setType(DataConstants.HTML_MIME_TYPE);
            description.setValue(contentBuffer.toString());
            entry.setSummary(description);
            
            entries.add(entry);
        }
        return entries;
    }
    
    private String getHtmlLink(String baseUrl, String repositoryName) {
        if (repositoryName == null || repositoryName.equals(WebConstants.ALL_REPO_FEEDS)) {
            return Utils.checkUrl(baseUrl.replace(WebConstants.REPO_SERVICE_PATH, "#/repos"));
        }
        else {
            return Utils.checkUrl(baseUrl.replace(WebConstants.REPO_SERVICE_PATH, "#/repo/" + repositoryName));
        }
    }
}


package org.bsdata.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
    
    private static HashMap<String, Date> lastCacheRefreshes;
    private static HashMap<String, HashMap<String, byte[]>> repoFileCache;
    private Indexer indexer;
    
    public GitHubDao() {
        indexer = new Indexer();
    }
    
    /**
     * Gets a connection to GitHub using the OAuth token in bsdata.properties
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
    
    private Repository getRepositoryFork(GitHubClient gitHubClient, String repositoryName) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        
        List<SearchRepository> searchRepositories = repositoryService.searchRepositories(
                repositoryName
                + " user:" + properties.getProperty(PropertiesConstants.GITHUB_USERNAME) 
                + " fork:only");
        
        if (searchRepositories.isEmpty()) {
            Repository repository = getBsDataRepository(gitHubClient, repositoryName);
            return repositoryService.forkRepository(repository);
        }
        
        return repositoryService.getRepository(
                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
                repositoryName);
    }
    
    private RepositoryVm createRepositoryVm(Repository repository, String baseUrl) {
        RepositoryVm repositoryVm = new RepositoryVm();
        repositoryVm.setName(repository.getName());
        repositoryVm.setDescription(repository.getDescription());
        repositoryVm.setRepoUrl(
                Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME));
        repositoryVm.setGitHubUrl(repository.getHtmlUrl());
        repositoryVm.setBugTrackerUrl(repository.getHtmlUrl() + "/issues");
        return repositoryVm;
    }
    
    /**
     * Ensures data files are cached for all data file repositories.
     * 
     * @param baseUrl
     * @throws IOException 
     */
    public synchronized void primeCache(String baseUrl) throws IOException {
        lastCacheRefreshes = null; // Clear the date map so it forces data to be re-cached.
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
    public synchronized HashMap<String, byte[]> getRepoFileData(
            String repositoryName, 
            String baseUrl, 
            List<String> repositoryUrls) throws IOException {
        
        if (lastCacheRefreshes == null) {
            lastCacheRefreshes = new HashMap<>();
        }
        if (repoFileCache == null) {
            repoFileCache = new HashMap<>();
        }

        Date lastCacheRefresh = lastCacheRefreshes.get(repositoryName);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        if (!repoFileCache.containsKey(repositoryName) 
                || lastCacheRefresh == null 
                || lastCacheRefresh.before(calendar.getTime())) {
            
            HashMap<String, byte[]> repositoryData = downloadFromGitHub(repositoryName);
            repositoryData = indexer.createRepositoryData(repositoryName, baseUrl, null, repositoryData);
            repoFileCache.put(repositoryName, repositoryData);
            lastCacheRefreshes.put(repositoryName, new Date());
        }
        
        return repoFileCache.get(repositoryName);
    }
    
    /**
     * Downloads all the data files from a particular data file repository.
     * 
     * @param repositoryName
     * @return
     * @throws IOException 
     */
    private HashMap<String, byte[]> downloadFromGitHub(String repositoryName) throws IOException {
        GitHubClient gitHubClient = connectToGitHub();
        ContentsService contentsService = new ContentsService(gitHubClient);
        DataService dataService = new DataService(gitHubClient);
        
        Repository repository = getBsDataRepository(gitHubClient, repositoryName);
        List<RepositoryContents> contents = contentsService.getContents(repository);
        HashMap<String, byte[]> repoFiles = new HashMap<>();
        for (RepositoryContents repositoryContents : contents) {
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
        
        List<Repository> orgRepositories = repositoryService.getOrgRepositories(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION));
        List<RepositoryVm> repositories = new ArrayList<>();
        for (Repository repository : orgRepositories) {
            if (repository.getName().equals(DataConstants.GITHUB_BSDATA_REPO_NAME)) {
                continue;
            }
            
            RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl);
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
        ContentsService contentsService = new ContentsService(gitHubClient);
        
        Repository repository = getBsDataRepository(gitHubClient, repositoryName);
        RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl);
        
        List<RepositoryContents> contents = contentsService.getContents(repository);
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
        Repository repositoryFork = getRepositoryFork(gitHubClient, repositoryName);
        
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
                + "_" + dateFormat.format(new Date());
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
        pullRequestService.createPullRequest(getBsDataRepository(gitHubClient, repositoryName), pullRequest);
    }
}

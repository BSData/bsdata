
package org.bsdata.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.viewmodel.RepositoryVm;
import org.bsdata.viewmodel.RepositoryFileVm;
import org.bsdata.viewmodel.RepositoryListVm;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.ApplicationProperties;
import org.bsdata.utils.Utils;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;


/**
 * Handles all reading and writing of data files to/from GitHub.
 * Methods in this class will generally convert GitHub objects to/from simpler objects from the viewmodel package.
 * These viewmodel objects can then be converted to/from JSON for sending/receiving over the wire.
 * 
 * @author Jonskichov
 */
public class GitHubDao {
    
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
        return gitHubClient;
    }
    
    private Repository getRepository(GitHubClient gitHubClient, String repositoryName) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        return repositoryService.getRepository(
                properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION), 
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
        
        Repository repository = getRepository(gitHubClient, repositoryName);
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
        
        Repository repository = getRepository(gitHubClient, repositoryName);
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
     * 
     * @param repositoryName
     * @param fileName
     * @param fileData
     * @throws IOException 
     */
    public void submitFile(String repositoryName, String fileName, byte[] fileData, String commitMessage) throws IOException {
//        if (Utils.isCompressedPath(fileName)) {
//            fileName = Utils.getUncompressedFileName(fileName);
//            fileData = Utils.decompressData(fileData);
//        }
//        
//        Properties properties = ApplicationProperties.getProperties();
//        GitHub gitHub = connectToGitHub();
//        
//        GHRepository ghRepository = gitHub
//                .getOrganization(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION))
//                .getRepository(repositoryName);
//        
//        String text = new String(fileData, "UTF-8");
//        text = text.replace("Warhammer", "WarhammerX");
//        
//        //createContent(text, commitMessage, fileName);//ghRepository.listCommits().iterator().next();
//        GHRef[] refs = ghRepository.getRefs();
//        
//        RepositoryService rs = new RepositoryService(ghc);
//        org.eclipse.egit.github.core.Repository r = rs.getRepository(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION), repositoryName);
//        rs.getBranches(r).get(0).
//        
//        CommitService cs = new CommitService(ghc);
//        cs.
//        ContentsService cs = new ContentsService(ghc);
//        cs.getContents(r).get(0).
//        
//        DataService ds = new DataService(ghc);
//        ds.getReference(ghRepository, "head")
//        DownloadService ds2 = new DownloadService(ghc);ds2.getDownloads(r).get(0).
        
        
        
        //r.
        // http://developer.github.com/v3/git/
        // get the current commit object
        // retrieve the tree it points to
        // retrieve the content of the blob object that tree has for that particular file path
        // change the content somehow and post a new blob object with that new content, getting a blob SHA back
        // post a new tree object with that file path pointer replaced with your new blob SHA getting a tree SHA back
        // create a new commit object with the current commit SHA as the parent and the new tree SHA, getting a commit SHA back
        // update the reference of your branch to point to the new commit SHA
    }
}

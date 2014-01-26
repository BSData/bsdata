
package org.bsdata.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.viewmodel.Repository;
import org.bsdata.viewmodel.RepositoryFile;
import org.bsdata.viewmodel.RepositoryFileList;
import org.bsdata.viewmodel.RepositoryList;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.ApplicationProperties;
import org.bsdata.utils.Utils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;


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
        lastCacheRefreshes = new HashMap<>();
    }
    
    private GitHub connectToGitHub() throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        
        return GitHub.connect(
                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
                properties.getProperty(PropertiesConstants.GITHUB_TOKEN));
        
//        GitHub gitHub = GitHub.connectUsingPassword(
//                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
//                properties.getProperty(PropertiesConstants.GITHUB_PASSWORD));
    }
    
    /**
     * Ensures data files are cached for all data file repositories.
     * 
     * @param baseUrl
     * @throws IOException 
     */
    public synchronized void primeCache(String baseUrl) throws IOException {
        lastCacheRefreshes = new HashMap<>(); // Recreate the date map so it forces data to be re-cached.
        RepositoryList repositoryList = getRepos(baseUrl);
        for (Repository repository : repositoryList.getRepositories()) {
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
        Properties properties = ApplicationProperties.getProperties();
        GitHub gitHub = connectToGitHub();
        
        GHRepository repository = gitHub
                .getOrganization(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION))
                .getRepository(repositoryName);
        List<GHContent> directoryContent = repository.getDirectoryContent("/");
        
        HashMap<String, byte[]> repoFiles = new HashMap<>();
        for (GHContent ghContent : directoryContent) {
            String content = ghContent.getEncodedContent();
            byte[] data = Base64.decodeBase64(content);
            repoFiles.put(ghContent.getName(), data);
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
    public RepositoryList getRepos(String baseUrl) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        GitHub gitHub = connectToGitHub();
        
        Map<String, GHRepository> ghRepositories = gitHub
                .getOrganization(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION))
                .getRepositories();
        
        List<Repository> repositories = new ArrayList<>();
        for (GHRepository ghRepository : ghRepositories.values()) {
            if (ghRepository.getName().equals(DataConstants.GITHUB_BSDATA_REPO_NAME)) {
                continue;
            }
            
            Repository repository = new Repository();
            repository.setName(ghRepository.getName());
            repository.setDescription(ghRepository.getDescription());
            String indexUrl = Utils.checkUrl(baseUrl + "/" + ghRepository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME);
            repository.setRepoUrl(indexUrl);
            repository.setGitHubUrl(ghRepository.getUrl());
            repository.setBugTrackerUrl(ghRepository.getUrl() + "/issues");
            repositories.add(repository);
        }
        
        // TODO: this should probably be cached...
        RepositoryList repositoryList = new RepositoryList();
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
    public RepositoryFileList getRepoFiles(String repositoryName, String baseUrl) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        GitHub gitHub = connectToGitHub();
        
        GHRepository ghRepository = gitHub
                .getOrganization(properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION))
                .getRepository(repositoryName);
        
        RepositoryFileList repositoryFileList = new RepositoryFileList();
        repositoryFileList.setName(ghRepository.getName());
        String indexUrl = Utils.checkUrl(baseUrl + "/" + ghRepository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME);
        repositoryFileList.setRepoUrl(indexUrl);
        repositoryFileList.setGitHubUrl(ghRepository.getUrl());
        
        List<GHContent> directoryContent = ghRepository.getDirectoryContent("/");
        List<RepositoryFile> repositoryFiles = new ArrayList<>();
        for (GHContent ghContent : directoryContent) {
            String fileName = Utils.getCompressedFileName(ghContent.getName());
            if (!Utils.isDataFilePath(fileName)) {
                continue;
            }
            
            RepositoryFile repositoryFile = new RepositoryFile();
            repositoryFile.setName(fileName);
            repositoryFile.setGitHubUrl(ghContent.getHtmlUrl());
            repositoryFile.setDataFileUrl(Utils.checkUrl(baseUrl + fileName));
            repositoryFiles.add(repositoryFile);
        }
        
        // TODO: this should probably be cached...
        repositoryFileList.setRepositoryFiles(repositoryFiles);
        return repositoryFileList;
    }
}

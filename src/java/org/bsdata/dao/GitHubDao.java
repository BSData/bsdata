/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.model.Repository;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.ApplicationProperties;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 *
 * @author Jonskichov
 */
public class GitHubDao {
    
    private static HashMap<String, HashMap<String, byte[]>> repoFileCache;
    
    public synchronized HashMap<String, byte[]> getRepoFiles(
            String repositoryName, 
            String baseUrl, 
            List<String> repositoryUrls) throws IOException {
        
        if (repoFileCache == null) {
            repoFileCache = new HashMap<>();
        }
        
        if (!repoFileCache.containsKey(repositoryName)) {
            HashMap<String, byte[]> repositoryData = downloadFromGitHub(repositoryName);
            repositoryData = Indexer.createRepositoryData(repositoryName, baseUrl, null, repositoryData);
            repoFileCache.put(repositoryName, repositoryData);
        }
        
        return repoFileCache.get(repositoryName);
    }
    
    private HashMap<String, byte[]> downloadFromGitHub(String repositoryName) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        
        GitHub gitHub = GitHub.connect(
                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
                properties.getProperty(PropertiesConstants.GITHUB_TOKEN));
        
//        GitHub gitHub = GitHub.connectUsingPassword(
//                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
//                properties.getProperty(PropertiesConstants.GITHUB_PASSWORD));
        
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
    
    public List<Repository> getRepositories(String baseUrl) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        
        GitHub gitHub = GitHub.connect(
                properties.getProperty(PropertiesConstants.GITHUB_USERNAME), 
                properties.getProperty(PropertiesConstants.GITHUB_TOKEN));
        
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
            repository.setRepoUrl(baseUrl + ghRepository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME);
            repository.setGitHubUrl(ghRepository.getUrl());
            repository.setBugTrackerUrl(ghRepository.getUrl() + "/issues");
            repositories.add(repository);
        }
        
        // TODO: this should probably be cached...
        return repositories;
    }
}

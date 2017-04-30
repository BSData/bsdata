
package org.bsdata.viewmodel;

import java.util.ArrayList;
import java.util.List;


public class RepositoryListVm extends ResponseVm {
    
    private String name;
    private String description;
    private String websiteUrl;
    private String repositoryListUrl;
    private String communityUrl;
    private String feedUrl;
    private String twitterUrl;
    private String facebookUrl;
    
    private List<RepositoryVm> repositories;
    
    
    public RepositoryListVm() {
        repositories = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getCommunityUrl() {
        return communityUrl;
    }

    public void setCommunityUrl(String communityUrl) {
        this.communityUrl = communityUrl;
    }

    public String getRepositoryListUrl() {
        return repositoryListUrl;
    }

    public void setRepositoryListUrl(String repositoryListUrl) {
        this.repositoryListUrl = repositoryListUrl;
    }

    public List<RepositoryVm> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryVm> repositories) {
        this.repositories = repositories;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}

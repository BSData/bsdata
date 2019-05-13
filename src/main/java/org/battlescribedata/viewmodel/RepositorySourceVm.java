
package org.battlescribedata.viewmodel;

import java.util.ArrayList;
import java.util.List;


public class RepositorySourceVm extends ResponseVm {
    
    private String name;
    private String description;
    private String battleScribeVersion;
    
    private String repositorySourceUrl;
    private String websiteUrl;
    private String githubUrl;
    private String discordUrl;
    private String feedUrl;
    private String twitterUrl;
    private String facebookUrl;
    
    private List<RepositoryVm> repositories;
    
    
    public RepositorySourceVm() {
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

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getRepositorySourceUrl() {
        return repositorySourceUrl;
    }

    public void setRepositorySourceUrl(String repositorySourceUrl) {
        this.repositorySourceUrl = repositorySourceUrl;
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
    
    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }
    
    public void setBattleScribeVersion(String battleScribeVersion) {
        this.battleScribeVersion = battleScribeVersion;
    }

    public String getDiscordUrl() {
        return discordUrl;
    }

    public void setDiscordUrl(String discordUrl) {
        this.discordUrl = discordUrl;
    }
}

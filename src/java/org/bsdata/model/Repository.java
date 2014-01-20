
package org.bsdata.model;


public class Repository {
    
    private String name;
    private String description;
    private String repoUrl;
    private String gitHubUrl;
    private String bugTrackerUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String url) {
        this.repoUrl = url;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    public String getBugTrackerUrl() {
        return bugTrackerUrl;
    }

    public void setBugTrackerUrl(String bugTrackerUrl) {
        this.bugTrackerUrl = bugTrackerUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

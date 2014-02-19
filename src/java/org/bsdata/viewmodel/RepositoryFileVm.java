
package org.bsdata.viewmodel;


public class RepositoryFileVm {
    
    private String name;
    private String gitHubUrl;
    private String dataFileUrl;
    private String errorMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    public String getDataFileUrl() {
        return dataFileUrl;
    }

    public void setDataFileUrl(String dataFileUrl) {
        this.dataFileUrl = dataFileUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
}

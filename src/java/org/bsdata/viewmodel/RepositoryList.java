
package org.bsdata.viewmodel;

import java.util.ArrayList;
import java.util.List;


public class RepositoryList {
    
    private List<Repository> repositories;
    private String errorMessage;
    
    public RepositoryList() {
        repositories = new ArrayList<>();
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}


package org.bsdata.model;

import java.util.ArrayList;
import java.util.List;


public class RepositoryFileList extends Repository {
    
    private String errorMessage;
    private List<RepositoryFile> repositoryFiles;
    
    public RepositoryFileList() {
        repositoryFiles = new ArrayList<>();
    }

    public List<RepositoryFile> getRepositoryFiles() {
        return repositoryFiles;
    }

    public void setRepositoryFiles(List<RepositoryFile> repositoryFiles) {
        this.repositoryFiles = repositoryFiles;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

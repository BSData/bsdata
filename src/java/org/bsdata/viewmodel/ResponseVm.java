/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.viewmodel;

/**
 *
 * @author Jonskichov
 */
public class ResponseVm {
    
    private String errorMessage;
    private String successMessage;
    private String responseUrl;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }
    
}

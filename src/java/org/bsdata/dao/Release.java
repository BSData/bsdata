/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.dao;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Jonskichov
 */
public class Release implements Serializable {
    
    private String name;
    private String tagName;
    private Date publishedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
    
}

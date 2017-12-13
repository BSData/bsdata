
package org.battlescribedata.model;

import java.io.Serializable;


public class DataFile implements Serializable {
    
    private transient byte[] data;
    
    private String id;
    private String name;
    private int revision;
    private String battleScribeVersion;
    private String authorName;
    private String authorContact;
    private String authorUrl;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }

    public void setBattleScribeVersion(String value) {
        this.battleScribeVersion = value;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorContact() {
        return authorContact;
    }

    public void setAuthorContact(String authorContact) {
        this.authorContact = authorContact;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}

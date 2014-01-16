
package org.bsdata.model;

import java.io.Serializable;
import java.util.ArrayList;


public class GameSystem implements Serializable {

    private String id;
    private int revision;
    private String battleScribeVersion;
    private String name;
    private String authorName;
    private String authorContact;
    private String authorUrl;
    
    public GameSystem () {
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int value) {
        this.revision = value;
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
}

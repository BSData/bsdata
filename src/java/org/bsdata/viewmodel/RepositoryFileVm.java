
package org.bsdata.viewmodel;


public class RepositoryFileVm extends ResponseVm {
    
    private String id;
    private String name;
    private String type;
    private int revision;
    private String battleScribeVersion;
    
    private String fileUrl;
    private String communityUrl;
    private String reportBugUrl;
    private String authorName;
    private String authorContact;
    private String authorUrl;

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommunityUrl() {
        return communityUrl;
    }

    public void setCommunityUrl(String communityUrl) {
        this.communityUrl = communityUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getReportBugUrl() {
        return reportBugUrl;
    }

    public void setReportBugUrl(String reportBugUrl) {
        this.reportBugUrl = reportBugUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }

    public void setBattleScribeVersion(String battleScribeVersion) {
        this.battleScribeVersion = battleScribeVersion;
    }
    
}

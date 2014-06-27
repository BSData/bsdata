
package org.bsdata.model;


public class Catalogue extends DataFile {

    private String id;
    private String gameSystemId;
    private int gameSystemRevision;
    private String battleScribeVersion;
    private String name;

    public Catalogue() {
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getGameSystemId() {
        return gameSystemId;
    }

    public void setGameSystemId(String value) {
        this.gameSystemId = value;
    }

    public int getGameSystemRevision() {
        return gameSystemRevision;
    }

    public void setGameSystemRevision(int value) {
        this.gameSystemRevision = value;
    }
}

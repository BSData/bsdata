
package org.bsdata.model;


public class GameSystem extends DataFile {

    private String id;
    private int revision;
    private String battleScribeVersion;
    private String name;
    
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
}

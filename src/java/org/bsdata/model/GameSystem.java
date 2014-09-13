
package org.bsdata.model;


public class GameSystem extends DataFile {

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

    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }

    public void setBattleScribeVersion(String value) {
        this.battleScribeVersion = value;
    }
}

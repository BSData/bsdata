
package org.battlescribedata.model;


public class Catalogue extends DataFile {
    
    private String gameSystemId;
    private int gameSystemRevision;

    public Catalogue() {
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

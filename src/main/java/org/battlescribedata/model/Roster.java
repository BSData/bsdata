
package org.battlescribedata.model;


public class Roster extends DataFile {
    
    private double points;
    private double pointsLimit;
    private String description;
    private String gameSystemId;
    private String gameSystemName;
    private int gameSystemRevision;

    public Roster() {
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double value) {
        this.points = value;
    }

    public double getPointsLimit() {
        return pointsLimit;
    }

    public void setPointsLimit(double value) {
        this.pointsLimit = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameSystemId() {
        return gameSystemId;
    }

    public void setGameSystemId(String gameSystemId) {
        this.gameSystemId = gameSystemId;
    }

    public String getGameSystemName() {
        return gameSystemName;
    }

    public void setGameSystemName(String gameSystemName) {
        this.gameSystemName = gameSystemName;
    }

    public int getGameSystemRevision() {
        return gameSystemRevision;
    }

    public void setGameSystemRevision(int gameSystemRevision) {
        this.gameSystemRevision = gameSystemRevision;
    }
}


package org.bsdata.model;


public class Roster extends DataFile {

    private String id;
    private String battleScribeVersion;
    private String name;
    private double points;
    private double pointsLimit;
    private String description;
    private String gameSystemId;
    private String gameSystemName;
    private int gameSystemRevision;

    public Roster() {
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
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

    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }

    public void setBattleScribeVersion(String value) {
        this.battleScribeVersion = value;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

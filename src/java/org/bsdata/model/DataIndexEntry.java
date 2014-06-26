
package org.bsdata.model;

import java.io.Serializable;
import java.util.Date;
import org.bsdata.constants.DataConstants.DataType;
import org.simpleframework.xml.Attribute;


public class DataIndexEntry implements Serializable {

    @Attribute
    private String filePath;
    @Attribute
    private String dataType;
    @Attribute
    private String dataId;
    @Attribute
    private String dataName;
    @Attribute
    private String dataBattleScribeVersion;
    @Attribute
    private int dataRevision;
    @Attribute(required = false)
    private Date lastModified;
    
    
    public DataIndexEntry(String filePath,
            String dataType,
            String dataId,
            String dataName,
            String dataBattleScribeVersion,
            int dataRevision) {
        
        this.filePath = filePath;
        this.dataType = dataType;
        this.dataId = dataId;
        this.dataName = dataName;
        this.dataBattleScribeVersion = dataBattleScribeVersion;
        this.dataRevision = dataRevision;
    }
    
    public DataIndexEntry(String filePath, DataFile dataFile) {
        this.filePath = filePath;
        
        if (dataFile instanceof GameSystem) {
            GameSystem gameSystem = (GameSystem)dataFile;
            
            this.dataType = DataType.GAME_SYSTEM.toString();
            this.dataId = gameSystem.getId();
            this.dataName = gameSystem.getName();
            this.dataBattleScribeVersion = gameSystem.getBattleScribeVersion();
            this.dataRevision = gameSystem.getRevision();
        }
        else if (dataFile instanceof Catalogue) {
            Catalogue catalogue = (Catalogue)dataFile;
            
            this.dataType = DataType.CATALOGUE.toString();
            this.dataId = catalogue.getId();
            this.dataName = catalogue.getName();
            this.dataBattleScribeVersion = catalogue.getBattleScribeVersion();
            this.dataRevision = catalogue.getRevision();
        }
        else if (dataFile instanceof Roster) {
            Roster roster = (Roster)dataFile;
            
            this.dataType = DataType.ROSTER.toString();
            this.dataId = roster.getId();
            this.dataName = roster.getName();
            this.dataBattleScribeVersion = roster.getBattleScribeVersion();
            this.dataRevision = 0;
        }
        else {
            throw new IllegalArgumentException("Not a data file");
        }
    }

    public String getDataBattleScribeVersion() {
        return dataBattleScribeVersion;
    }

    public void setDataBattleScribeVersion(String dataBattleScribeVersion) {
        this.dataBattleScribeVersion = dataBattleScribeVersion;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public int getDataRevision() {
        return dataRevision;
    }

    public void setDataRevision(int dataRevision) {
        this.dataRevision = dataRevision;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}

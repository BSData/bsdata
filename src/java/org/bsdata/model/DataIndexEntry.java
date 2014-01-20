
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
    
    public DataIndexEntry(String filePath, GameSystem gameSystem) {
        this(filePath, 
                DataType.GAME_SYSTEM.toString(),
                gameSystem.getId(),
                gameSystem.getName(),
                gameSystem.getBattleScribeVersion(),
                gameSystem.getRevision());
    }
    
    public DataIndexEntry(String filePath, GameSystem gameSystem, Date lastModified) {
        this (filePath, gameSystem);
        this.lastModified = lastModified;
    }
    
    public DataIndexEntry(String filePath, Catalogue catalogue) {
        this(filePath, 
                DataType.CATALOGUE.toString(),
                catalogue.getId(),
                catalogue.getName(),
                catalogue.getBattleScribeVersion(),
                catalogue.getRevision());
    }
    
    public DataIndexEntry(String filePath, Catalogue catalogue, Date lastModified) {
        this (filePath, catalogue);
        this.lastModified = lastModified;
    }
    
    public DataIndexEntry(String filePath, Roster roster) {
        this(filePath, 
                DataType.ROSTER.toString(),
                roster.getId(),
                roster.getName(),
                roster.getBattleScribeVersion(),
                0);
    }
    
    public DataIndexEntry(String filePath, Roster roster, Date lastModified) {
        this (filePath, roster);
        this.lastModified = lastModified;
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


package org.battlescribedata.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.battlescribedata.utils.Utils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


@Root
@Namespace(reference = "http://www.battlescribe.net/schema/dataIndexSchema")
public class DataIndex extends DataFile {

    @Attribute
    private String battleScribeVersion;
    @Attribute
    private String name;
    @Attribute(required = false)
    private String indexUrl;
    @ElementList(required = false, entry = "repositoryUrl")
    private ArrayList<String> repositoryUrls;
    @ElementList(required = false)
    private ArrayList<DataIndexEntry> dataIndexEntries;

    public DataIndex(String name, String indexUrl) {
        this.battleScribeVersion = "1.13b";
        this.name = name;
        indexUrl = Utils.checkUrl(indexUrl);
        if (!StringUtils.isEmpty(indexUrl)) {
            this.indexUrl = indexUrl;
        }
        
        this.dataIndexEntries = new ArrayList<>();
        this.repositoryUrls = new ArrayList<>();
    }

    public DataIndex(String name, String indexUrl, List<String> repositoryUrls) {
        this(name, indexUrl);
        
        if (repositoryUrls != null) {
            for (String url : repositoryUrls) {
                url = Utils.checkUrl(url);
                if (StringUtils.isEmpty(url) || this.repositoryUrls.contains(url)) {
                    continue;
                }
                this.repositoryUrls.add(url);
            }
        }
    }

    public String getBattleScribeVersion() {
        return battleScribeVersion;
    }

    public void setBattleScribeVersion(String battleScribeVersion) {
        this.battleScribeVersion = battleScribeVersion;
    }

    public ArrayList<DataIndexEntry> getDataIndexEntries() {
        return dataIndexEntries;
    }

    public void setDataIndexEntries(ArrayList<DataIndexEntry> entries) {
        this.dataIndexEntries = entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getRepositoryUrls() {
        return repositoryUrls;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }
}

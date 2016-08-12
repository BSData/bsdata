/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.constants;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jonskichov
 */
public class DataConstants {
    
    public static final String GITHUB_BSDATA_REPO_NAME = "bsdata";
    
    public static final String REPOSITORY_FILE_MIME_TYPE = "application/battlescribe.bsr";
    public static final String INDEX_FILE_MIME_TYPE = "application/battlescribe.bsi";
    public static final String ROSTER_FILE_MIME_TYPE = "application/battlescribe.rosz";
    public static final String CATALOGUE_FILE_MIME_TYPE = "application/battlescribe.catz";
    public static final String GAME_SYSTEM_FILE_MIME_TYPE = "application/battlescribe.gstz";
    public static final String OCTETSTREAM_MIME_TYPE = "application/octet-stream";
    public static final String ZIP_MIME_TYPE = "application/zip";
    public static final String TEXT_MIME_TYPE = "text/plain";
    public static final String HTML_MIME_TYPE = "text/html";
    
    public static final String ROSTER_FILE_EXTENSION = ".ros";
    public static final String CATALOGUE_FILE_EXTENSION = ".cat";
    public static final String GAME_SYSTEM_FILE_EXTENSION = ".gst";
    
    public static final String ROSTER_COMPRESSED_FILE_EXTENSION = ".rosz";
    public static final String CATALOGUE_COMPRESSED_FILE_EXTENSION = ".catz";
    public static final String GAME_SYSTEM_COMPRESSED_FILE_EXTENSION = ".gstz";
    
    public static final String ROSTER_COMPRESSED_FILE_EXTENSION_OLD = ".ros.zip";
    public static final String CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD = ".cat.zip";
    public static final String GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD = ".gst.zip";
    
    public static final String ZIP_FILE_EXTENSION = ".zip";
    public static final String REPOSITORY_FILE_EXTENSION = ".bsr";
    public static final String INDEX_COMPRESSED_FILE_EXTENSION = ".bsi";
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String DEFAULT_INDEX_COMPRESSED_FILE_NAME = "index.bsi";
    public static final String DEFAULT_INDEX_FILE_NAME = "index.xml";

    public static final String BATTLESCRIBE_VERSION_ATTRIBUTE = "battleScribeVersion";
    public static final String REVISION_ATTRIBUTE = "revision";
    public static final String ID_ATTRIBUTE = "id";
    public static final String AUTHOR_NAME_ATTRIBUTE = "authorName";
    public static final String AUTHOR_CONTACT_ATTRIBUTE = "authorContact";
    public static final String AUTHOR_URL_ATTRIBUTE = "authorUrl";
    public static final String GAME_SYSTEM_ID_ATTRIBUTE = "gameSystemId";
    public static final String GAME_SYSTEM_NAME_ATTRIBUTE = "gameSystemName";
    public static final String GAME_SYSTEM_REVISION_ATTRIBUTE = "gameSystemRevision";
    public static final String CATALOGUE_ID_ATTRIBUTE = "catalogueId";
    public static final String FORCE_TYPE_ID_ATTRIBUTE = "forceTypeId";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String CATALOGUE_NAME_ATTRIBUTE = "catalogueName";
    public static final String CATALOGUE_REVISION_ATTRIBUTE = "catalogueRevision";
    public static final String FORCE_TYPE_NAME_ATTRIBUTE = "forceTypeName";
    public static final String POINTS_ATTRIBUTE = "points";
    public static final String POINTS_LIMIT_ATTRIBUTE = "pointsLimit";
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    public static final String ROSTER_TAG = "roster";
    public static final String CATALOGUE_TAG = "catalogue";
    public static final String GAME_SYSTEM_TAG = "gameSystem";
    public static final String CATALOGUE_LINK_TAG = "catalogueLink";
    public static final String CATALOGUE_LINKS_TAG = "catalogueLinks";

    public enum DataType {

        ROSTER("roster"),
        CATALOGUE("catalogue"),
        GAME_SYSTEM("gamesystem");
        
        private final String value;

        DataType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static DataType fromString(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            for (DataType dataType : DataType.values()) {
                if (dataType.toString().equalsIgnoreCase(value)) {
                    return dataType;
                }
            }
            return null;
        }

        public static List<String> getStrings() {
            ArrayList<String> strings = new ArrayList<>();
            for (DataType dataType : DataType.values()) {
                strings.add(dataType.toString());
            }
            return strings;
        }
    }
}
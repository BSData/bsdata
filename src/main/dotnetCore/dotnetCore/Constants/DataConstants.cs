using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.constants
{
    /**
     * constants relating to BattleScribe data files
     */
    public static class DataConstants
    {
        public const string CURRENT_DATA_FORMAT_VERSION = "2.02";
        public const string MIN_DATA_FORMAT_VERSION = "1.13b";

        // MIME types
        public const string REPOSITORY_FILE_MIME_TYPE = "application/battlescribe.bsr";
        public const string INDEX_FILE_MIME_TYPE = "application/battlescribe.bsi";
        public const string ROSTER_FILE_MIME_TYPE = "application/battlescribe.rosz";
        public const string CATALOGUE_FILE_MIME_TYPE = "application/battlescribe.catz";
        public const string GAME_SYSTEM_FILE_MIME_TYPE = "application/battlescribe.gstz";
        public const string OCTETSTREAM_MIME_TYPE = "application/octet-stream";
        public const string ZIP_MIME_TYPE = "application/zip";
        public const string TEXT_MIME_TYPE = "text/plain";
        public const string HTML_MIME_TYPE = "text/html";

        // File names/extensions
        public const string ROSTER_FILE_EXTENSION = ".ros";
        public const string CATALOGUE_FILE_EXTENSION = ".cat";
        public const string GAME_SYSTEM_FILE_EXTENSION = ".gst";
    
        public const string ROSTER_COMPRESSED_FILE_EXTENSION = ".rosz";
        public const string CATALOGUE_COMPRESSED_FILE_EXTENSION = ".catz";
        public const string GAME_SYSTEM_COMPRESSED_FILE_EXTENSION = ".gstz";
    
        public const string ROSTER_COMPRESSED_FILE_EXTENSION_OLD = ".ros.zip";
        public const string CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD = ".cat.zip";
        public const string GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD = ".gst.zip";
    
        public const string ZIP_FILE_EXTENSION = ".zip";
        public const string REPOSITORY_FILE_EXTENSION = ".bsr";
        public const string INDEX_COMPRESSED_FILE_EXTENSION = ".bsi";
        public const string XML_FILE_EXTENSION = ".xml";
        public const string DEFAULT_INDEX_COMPRESSED_FILE_NAME = "index.bsi";
        public const string DEFAULT_INDEX_FILE_NAME = "index.xml";

        // XML tag/attribute names
        public const string BATTLESCRIBE_VERSION_ATTRIBUTE = "battleScribeVersion";
        public const string REVISION_ATTRIBUTE = "revision";
        public const string ID_ATTRIBUTE = "id";
        public const string AUTHOR_NAME_ATTRIBUTE = "authorName";
        public const string AUTHOR_CONTACT_ATTRIBUTE = "authorContact";
        public const string AUTHOR_URL_ATTRIBUTE = "authorUrl";
        public const string GAME_SYSTEM_ID_ATTRIBUTE = "gameSystemId";
        public const string GAME_SYSTEM_NAME_ATTRIBUTE = "gameSystemName";
        public const string GAME_SYSTEM_REVISION_ATTRIBUTE = "gameSystemRevision";
        public const string CATALOGUE_ID_ATTRIBUTE = "catalogueId";
        public const string FORCE_TYPE_ID_ATTRIBUTE = "forceTypeId";
        public const string NAME_ATTRIBUTE = "name";
        public const string CATALOGUE_NAME_ATTRIBUTE = "catalogueName";
        public const string CATALOGUE_REVISION_ATTRIBUTE = "catalogueRevision";
        public const string FORCE_TYPE_NAME_ATTRIBUTE = "forceTypeName";
        public const string POINTS_ATTRIBUTE = "points";
        public const string POINTS_LIMIT_ATTRIBUTE = "pointsLimit";
        public const string DESCRIPTION_ATTRIBUTE = "description";
        public const string ROSTER_TAG = "roster";
        public const string CATALOGUE_TAG = "catalogue";
        public const string GAME_SYSTEM_TAG = "gameSystem";
        public const string CATALOGUE_LINK_TAG = "catalogueLink";
        public const string CATALOGUE_LINKS_TAG = "catalogueLinks";

        public class DataType
        {
            public const string ROSTER = "roster";
            public const string CATALOGUE = "catalogue";
            public const string GAME_SYSTEM = "gamesystem";
        }

    }
}

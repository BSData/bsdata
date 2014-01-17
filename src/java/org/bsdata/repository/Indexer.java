
package org.bsdata.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.model.Catalogue;
import org.bsdata.model.DataIndex;
import org.bsdata.model.DataIndexEntry;
import org.bsdata.model.GameSystem;
import org.bsdata.model.Roster;
import org.bsdata.utils.Utils;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class Indexer {
    
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static Persister persister;
    
    private static Persister getPersister() {
        if (persister == null) {
            persister = new Persister(new Format(2, XML_DECLARATION));
        }
        return persister;
    }

    private static ByteArrayInputStream writeDataIndex(DataIndex dataIndex) throws IOException, XmlException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            getPersister().write(dataIndex, outputStream, "UTF-8");
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        catch (Exception ex) {
            throw new XmlException(ex);
        }
    }
    
    public static HashMap<String, byte[]> getRepoFiles(
            String repositoryName, 
            String indexUrl, 
            List<String> repositoryUrls, 
            HashMap<String, ByteArrayInputStream> dataFiles) throws IOException {
        
        DataIndex dataIndex = createDataIndex(repositoryName, indexUrl, repositoryUrls, dataFiles);
        dataFiles.put(DataConstants.DEFAULT_INDEX_FILE_NAME, writeDataIndex(dataIndex));
        return compressRepoFiles(dataFiles);
    }
    
    /**
     * Returns a new map of fileName to inputStream ensuring all file names are compressed names and all data is compressed
     * When an uncompressed file name is encountered, the associated inputStream is compressed.
     * 
     * @param dataFiles
     * @return
     * @throws IOException 
     */
    private static HashMap<String, byte[]> compressRepoFiles(HashMap<String, ByteArrayInputStream> dataFiles) throws IOException {
        HashMap<String, byte[]> compressedDataFiles = new HashMap<>();
        for (String fileName : dataFiles.keySet()) {
            ByteArrayInputStream inputStream = dataFiles.get(fileName);

            if (Utils.isCompressedPath(fileName)) {
                // Data already compressed - just copy it to a byte[]
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(inputStream, outputStream);
                fileName = FilenameUtils.getName(fileName);
                compressedDataFiles.put(fileName, outputStream.toByteArray());
            }
            else {
                // We need to compress the data
                byte[] compressedData = Utils.compressInputStream(fileName, inputStream);
                fileName = Utils.getCompressedFileName(fileName);
                compressedDataFiles.put(fileName, compressedData);
            }
        }
        return compressedDataFiles;
    }

    /**
     * Create a data index from a set of data files
     * Ensures the resulting data index entries use compressed file names
     * 
     * @param repositoryName A name for the repo
     * @param indexUrl The URL the index.bsi will be hosted at
     * @param repositoryUrls Optional list of repo URLs to include in the index
     * @param dataFiles The data files to make the index from
     * 
     * @return
     * @throws IOException
     * @throws XmlException 
     */
    private static DataIndex createDataIndex(
            String repositoryName, 
            String indexUrl, 
            List<String> repositoryUrls, 
            HashMap<String, ByteArrayInputStream> dataFiles)
            throws IOException, XmlException {

        DataIndex dataIndex = new DataIndex(repositoryName, indexUrl, repositoryUrls);

        for (String fileName : dataFiles.keySet()) {
            ByteArrayInputStream inputStream = dataFiles.get(fileName);
            if (Utils.isCompressedPath(fileName)) {
                // Decompress the stream if we need to so we can read the XML
                inputStream = Utils.decompressStream(inputStream);
            }
            
            // Make sure we use a compressed file names in the index. 
            // This will also ensure it's just the filename, not full path.
            fileName = Utils.getCompressedFileName(fileName);
            
            try {
                // Create a data index entry and add it to our data index
                DataIndexEntry dataIndexEntry;
                
                if (Utils.isGameSytstemPath(fileName)) {
                    GameSystem gameSystem = readGameSystem(inputStream);
                    dataIndexEntry = new DataIndexEntry(fileName, gameSystem);
                }
                else if (Utils.isCataloguePath(fileName)) {
                    Catalogue catalogue = readCatalogue(inputStream);
                    dataIndexEntry = new DataIndexEntry(fileName, catalogue);
                }
                else if (Utils.isRosterPath(fileName)) {
                    Roster roster = readRoster(inputStream);
                    dataIndexEntry = new DataIndexEntry(fileName, roster);
                }
                else {
                    continue;
                }
                
                dataIndex.getDataIndexEntries().add(dataIndexEntry);
            }
            catch (IOException e) {
                // TODO: handle exception
            }
        }
        
        return dataIndex;
    }

    /**
     * Buffers internally
     * 
     * @param inputStream
     * @return
     * @throws XmlException 
     */
    private static Catalogue readCatalogue(ByteArrayInputStream inputStream) throws XmlException, IOException {
        final Catalogue catalogue = new Catalogue();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase(DataConstants.CATALOGUE_TAG)) {
                        catalogue.setId(attributes.getValue(DataConstants.ID_ATTRIBUTE));
                        catalogue.setGameSystemId(attributes.getValue(DataConstants.GAME_SYSTEM_ID_ATTRIBUTE));
                        catalogue.setBattleScribeVersion(attributes.getValue(DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE));
                        catalogue.setRevision(Integer.parseInt(attributes.getValue(DataConstants.REVISION_ATTRIBUTE)));
                        catalogue.setName(attributes.getValue(DataConstants.NAME_ATTRIBUTE));
                        catalogue.setAuthorName(attributes.getValue(DataConstants.AUTHOR_NAME_ATTRIBUTE));
                        catalogue.setAuthorContact(attributes.getValue(DataConstants.AUTHOR_CONTACT_ATTRIBUTE));
                        catalogue.setAuthorUrl(attributes.getValue(DataConstants.AUTHOR_URL_ATTRIBUTE));
                        throw new SAXParseCompleteException("DONE");
                    }
                }
            });
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        catch (SAXParseCompleteException e) {
            return catalogue;
        }
        catch (SAXException ex) {
            throw new XmlException(ex);
        }
        throw new XmlException("Invalid catalogue XML");
    }

    /**
     * Buffered internally.
     * 
     * @param inputStream
     * @return
     * @throws XmlException
     * @throws IOException 
     */
    private static GameSystem readGameSystem(ByteArrayInputStream inputStream) throws XmlException, IOException {
        final GameSystem gameSystem = new GameSystem();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase(DataConstants.GAME_SYSTEM_TAG)) {
                        gameSystem.setId(attributes.getValue(DataConstants.ID_ATTRIBUTE));
                        gameSystem.setBattleScribeVersion(attributes.getValue(DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE));
                        gameSystem.setRevision(Integer.parseInt(attributes.getValue(DataConstants.REVISION_ATTRIBUTE)));
                        gameSystem.setName(attributes.getValue(DataConstants.NAME_ATTRIBUTE));
                        gameSystem.setAuthorName(attributes.getValue(DataConstants.AUTHOR_NAME_ATTRIBUTE));
                        gameSystem.setAuthorContact(attributes.getValue(DataConstants.AUTHOR_CONTACT_ATTRIBUTE));
                        gameSystem.setAuthorUrl(attributes.getValue(DataConstants.AUTHOR_URL_ATTRIBUTE));
                        throw new SAXParseCompleteException("DONE");
                    }
                }
            });
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        catch (SAXParseCompleteException e) {
            return gameSystem;
        }
        catch (SAXException ex) {
            throw new XmlException(ex);
        }
        throw new XmlException("Invalid catalogue XML");
    }

    /**
     * Buffered internally.
     * 
     * @param inputStream
     * @return
     * @throws XmlException
     * @throws IOException 
     */
    private static Roster readRoster(ByteArrayInputStream inputStream) throws XmlException, IOException {
        final Roster roster = new Roster();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase(DataConstants.ROSTER_TAG)) {
                        roster.setBattleScribeVersion(attributes.getValue(DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE));
                        roster.setDescription(attributes.getValue(DataConstants.DESCRIPTION_ATTRIBUTE));
                        roster.setName(attributes.getValue(DataConstants.NAME_ATTRIBUTE));
                        roster.setPoints(Double.parseDouble(attributes.getValue(DataConstants.POINTS_ATTRIBUTE)));
                        roster.setPointsLimit(Double.parseDouble(attributes.getValue(DataConstants.POINTS_LIMIT_ATTRIBUTE)));
                        roster.setGameSystemId(attributes.getValue(DataConstants.GAME_SYSTEM_ID_ATTRIBUTE));
                        
                        if (attributes.getIndex(DataConstants.GAME_SYSTEM_NAME_ATTRIBUTE) >= 0) {
                            roster.setGameSystemName(attributes.getValue(DataConstants.GAME_SYSTEM_NAME_ATTRIBUTE));
                        }
                        if (attributes.getIndex(DataConstants.GAME_SYSTEM_REVISION_ATTRIBUTE) >= 0) {
                            roster.setGameSystemRevision(
                                    Integer.parseInt(attributes.getValue(DataConstants.GAME_SYSTEM_REVISION_ATTRIBUTE)));
                        }
                        
                        throw new SAXParseCompleteException("DONE");
                    }
                }
            });
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        catch (SAXParseCompleteException e) {
            return roster;
        }
        catch (SAXException ex) {
            throw new XmlException(ex);
        }
        throw new XmlException("Invalid roster XML");
    }
}

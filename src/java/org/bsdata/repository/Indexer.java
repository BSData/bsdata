
package org.bsdata.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.bsdata.constants.DataConstants;
import org.bsdata.model.Catalogue;
import org.bsdata.model.DataFile;
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


/**
 * Handles creating a BattleScribe repository index from a set of data files.
 * 
 * @author Jonskichov
 */
public class Indexer {
    
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static Persister persister;
    
    /**
     * The SimpleXML persister used to read/write a data index object to/from XML.
     * 
     * @return 
     */
    private static Persister getPersister() {
        if (persister == null) {
            persister = new Persister(new Format(2, XML_DECLARATION));
        }
        return persister;
    }

    /**
     * Writes a data file repository index to an in-memory XML file.
     * 
     * @param dataIndex
     * @return
     * @throws IOException
     * @throws XmlException 
     */
    private byte[] writeDataIndex(DataIndex dataIndex) throws IOException, XmlException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            getPersister().write(dataIndex, outputStream, "UTF-8");
            return outputStream.toByteArray();
        }
        catch (Exception ex) {
            throw new XmlException(ex);
        }
    }
    
    /**
     * Returns a complete data file repository from a set of data files (including the index) from a particular GitHub repository.
     * 
     * 1) A data file repository index file is created from the data files.
     * 2) All data files and the index file are compressed.
     * 3) All file names are ensured to be the compressed format.
     * 4) A HashMap of compressed data file name to compressed file data is created.
     * 5) The HashMap is returned so it can be cached and/or served.
     * 
     * @param repositoryName
     * @param baseUrl
     * @param repositoryUrls List of additional repo URLs to add to the index
     * @param dataFiles Map of uncompressed file name to uncompressed file data
     * @return
     * @throws IOException 
     */
    public HashMap<String, DataFile> createRepositoryData(
            String repositoryName, 
            String baseUrl, 
            List<String> repositoryUrls, 
            HashMap<String, byte[]> fileDatas)
            throws IOException, XmlException {

        String indexUrl = Utils.checkUrl(baseUrl + "/" + repositoryName + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME);
        DataIndex dataIndex = new DataIndex(repositoryName, indexUrl, repositoryUrls);
        
        HashMap<String, DataFile> repositoryData = new HashMap<>();

        for (String fileName : fileDatas.keySet()) {
            if (Utils.isCompressedPath(fileName)) {
                // We should only get uncompressed data at this point
                throw new IllegalArgumentException("Data file " + fileName + " is already compressed.");
            }
            
            byte[] fileData = fileDatas.get(fileName);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
            
            DataFile dataFile;
            if (Utils.isGameSytstemPath(fileName)) {
                dataFile = readGameSystem(inputStream);
            }
            else if (Utils.isCataloguePath(fileName)) {
                dataFile = readCatalogue(inputStream);
            }
            else if (Utils.isRosterPath(fileName)) {
                dataFile = readRoster(inputStream);
            }
            else {
                continue;
            }
            
            // Compress the file data and set it on the DataFile
            fileData = Utils.compressData(fileName, fileData);
            dataFile.setData(fileData);

            // Create a DataIndexEntry using compressed file name
            fileName = Utils.getCompressedFileName(fileName);
            DataIndexEntry dataIndexEntry = new DataIndexEntry(fileName, dataFile);
            
            // Add our data file and index entry
            repositoryData.put(fileName, dataFile);
            dataIndex.getDataIndexEntries().add(dataIndexEntry);
        }
        
        // Compress the index file data and set it on the DataIndex
        byte[] indexData = writeDataIndex(dataIndex);
        indexData = Utils.compressData(DataConstants.DEFAULT_INDEX_FILE_NAME, indexData);
        dataIndex.setData(indexData);
        
        // Add the DataIndex to the hashmap
        repositoryData.put(DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME, dataIndex);
        
        return repositoryData;
    }

    /**
     * Buffers internally
     * 
     * @param inputStream
     * @return
     * @throws XmlException 
     */
    private Catalogue readCatalogue(ByteArrayInputStream inputStream) throws XmlException, IOException {
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
    private GameSystem readGameSystem(ByteArrayInputStream inputStream) throws XmlException, IOException {
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
    private Roster readRoster(ByteArrayInputStream inputStream) throws XmlException, IOException {
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

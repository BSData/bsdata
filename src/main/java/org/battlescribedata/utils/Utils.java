
package org.battlescribedata.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.battlescribedata.constants.DataConstants;
import org.battlescribedata.constants.FileConstants;
import org.battlescribedata.repository.SAXParseCompleteException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class Utils {
    
    public static final int BUFFER_SIZE = 1024 * 64;
    

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }
    
    public static String checkUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        url = url.trim().replace(" ", "%20");
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        
        if (url.equals("http://")) {
            return null;
        }

        try {
            URL urlObj = new URL(url);
        }
        catch (MalformedURLException e) {
            return null;
        }

        return url;
    }
    
    /**
     * Downloads the file at the given URL.
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public static byte[] downloadFile(String url) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(5 * 1000);
            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Connection", "close");
            
            inputStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, outputStream);
            
            return outputStream.toByteArray();
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    public static String getBaseUrl(String url) {
        return url.substring(0, url.lastIndexOf('/') + 1);
    }
    
    public static boolean isDataFilePath(String path) {
        return (isRosterPath(path) || isCataloguePath(path) || isGameSytstemPath(path));
    }

    public static boolean isRosterFile(File file) {
        return isRosterPath(file.getName());
    }

    public static boolean isRosterPath(String path) {
        path = path.trim().toLowerCase();
        return path.endsWith(DataConstants.ROSTER_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD);
    }

    public static boolean isCatalogueFile(File file) {
        return isCataloguePath(file.getName());
    }

    public static boolean isCataloguePath(String path) {
        path = path.trim().toLowerCase();
        return path.endsWith(DataConstants.CATALOGUE_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD);
    }
    
    public static boolean isGameSytstemFile(File file) {
        return isGameSytstemPath(file.getName());
    }

    public static boolean isGameSytstemPath(String path) {
        path = path.trim().toLowerCase();
        return path.endsWith(DataConstants.GAME_SYSTEM_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD);
    }

    public static boolean isIndexFile(File file) {
        return isIndexPath(file.getName());
    }

    public static boolean isIndexPath(String path) {
        path = path.trim().toLowerCase();
        return path.endsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.DEFAULT_INDEX_FILE_NAME);
    }

    public static boolean isCompressedFile(File file) {
        return isCompressedPath(file.getName());
    }
    
    public static boolean isCompressedPath(String path) {
        path = path.trim().toLowerCase();
        return path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION);
    }
    
    public static String getCompressedFileName(File file) {
        return getCompressedFileName(file.getName());
    }
    
    /**
     * Returns a filename (not full path!) with a compressed file extension (.gstz/.catz/.rosz/.bsi)
     * 
     * @param fileName
     * @return 
     */
    public static String getCompressedFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        
        fileName = FilenameUtils.getName(fileName);
        
        // LEGACY FILE NAMES //
        if (fileName.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD)) {
            fileName = fileName.replace(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION);
        }
        else if (fileName.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD)) {
            fileName = fileName.replace(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION);
        }
        else if (fileName.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD)) {
            fileName = fileName.replace(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION);
        }
        
        if (isCompressedPath(fileName)) {
            return fileName;
        }
        
        if (isGameSytstemPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION;
        }
        else if (isCataloguePath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION;
        }
        else if (isRosterPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION;
        }
        else if (isIndexPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.INDEX_COMPRESSED_FILE_EXTENSION;
        }
        
        return fileName;
    }
    
    public static String getUncompressedFileName(File file) {
        return getUncompressedFileName(file.getName());
    }
    
    /**
     * Returns a filename (not full path!) with an uncompressed file extension (.gst/.cat/.ros/.xml)
     * 
     * @param fileName
     * @return 
     */
    public static String getUncompressedFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        
        fileName = FilenameUtils.getName(fileName);
        
        if (!isCompressedPath(fileName)) {
            return fileName;
        }
        
        if (isGameSytstemPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.GAME_SYSTEM_FILE_EXTENSION;
        }
        else if (isCataloguePath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.CATALOGUE_FILE_EXTENSION;
        }
        else if (isRosterPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.ROSTER_FILE_EXTENSION;
        }
        else if (isIndexPath(fileName)) {
            fileName = FilenameUtils.getBaseName(fileName) + DataConstants.XML_FILE_EXTENSION;
        }
        return fileName;
    }

    /**
     * Buffers internally.
     * Closes inputStream.
     * 
     * @param inputStream
     * @return
     * @throws IOException 
     */
    public static ByteArrayInputStream readStreamToMemory(InputStream inputStream) throws IOException {
        if (inputStream instanceof ByteArrayInputStream) {
            inputStream.reset();
            return (ByteArrayInputStream) inputStream;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 80);
            IOUtils.copy(inputStream, outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    public static ByteArrayInputStream readPartialStream(InputStream inputStream, boolean resetInputStream) throws IOException {
        if (!inputStream.markSupported()) {
            throw new IllegalArgumentException();
        }
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream.mark(BUFFER_SIZE);
        }
        
        try {
            int count = 0;
            int n = 0;
            int tempBufferSize = 1024 * 8;
            byte[] buffer = new byte[tempBufferSize];
            ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
            while (-1 != (n = inputStream.read(buffer)) && (count + tempBufferSize) < BUFFER_SIZE) {
                if (n == 0) {
                    throw new IOException("Read returned 0 bytes.");
                }
                output.write(buffer, 0, n);
                count += n;
            }
            return new ByteArrayInputStream(output.toByteArray());
        }
        catch (EOFException e) {
            throw new IOException("Reached EOF", e);
        }
        finally {
            if (resetInputStream) {
                resetStreamQuietly(inputStream);
            }
            else {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
    
    private static void resetStreamQuietly(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        
        if (!inputStream.markSupported()) {
            IOUtils.closeQuietly(inputStream);
            return;
        }
        
        try {
            inputStream.reset();
        }
        catch (IOException e) {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    
    /**
     * Assumes there is only one ZipEntry in the stream.
     * Buffers internally.
     * Closes inputStream.
     * 
     * @param inputStream
     * @return 
     * @throws java.io.IOException 
     */
    public static ByteArrayInputStream decompressStream(ByteArrayInputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = getDecompressedInputStream(inputStream);
        return readStreamToMemory(zipInputStream);
    }
    
    public static byte[] decompressData(byte[] data) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ZipInputStream zipInputStream = getDecompressedInputStream(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        IOUtils.copy(zipInputStream, outputStream);
        
        return outputStream.toByteArray();
    }
    
    private static ZipInputStream getDecompressedInputStream(ByteArrayInputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        zipInputStream.getNextEntry();
        return zipInputStream;
    }
    
    /**
     * Compress the given data array using the zipEntryName
     * 
     * @param zipEntryName
     * @param data
     * @return
     * @throws IOException 
     */
    public static byte[] compressData(String zipEntryName, byte[] data) throws IOException {
        if (Utils.isCompressedPath(zipEntryName)) {
            throw new IllegalArgumentException("Zip entry name must have an uncompressed file extention");
        }
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = getCompressedOutputStream(zipEntryName, outputStream);

        IOUtils.copy(inputStream, zipOutputStream);
        zipOutputStream.flush();
        zipOutputStream.finish();

        return outputStream.toByteArray();
    }
    
    private static ZipOutputStream getCompressedOutputStream(String zipEntryName, ByteArrayOutputStream outputStream) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        ZipEntry zipEntry = new ZipEntry(getUncompressedFileName(zipEntryName));
        zipOutputStream.putNextEntry(zipEntry);
        return zipOutputStream;
    }
    
    /**
     * Decompress all zip entries from the stream
     * 
     * @param data
     * @return
     * @throws IOException 
     */
    public static HashMap<String, byte[]> unpackZip(byte[] data) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                
        HashMap<String, byte[]> zipData = new HashMap<>();
        
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                if (fileName.startsWith("/")) {
                    fileName = fileName.substring(1);
                }
                
                // Can't use readStreamToMemory because it closes zipInputStream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 80);
                IOUtils.copy(zipInputStream, outputStream);
                zipData.put(fileName, outputStream.toByteArray());
                
                zipEntry = zipInputStream.getNextEntry();
            }
        }
        finally {
            IOUtils.closeQuietly(zipInputStream);
        }
        
        return zipData;
    }
    
    
    ///////////////////
    // Data Upgrades //
    ///////////////////

    /**
     * Buffers internally.Closes inputStream.
     * 
     * @param data
     * @param filePath
     * @return
     * @throws IOException 
     */
    @SuppressWarnings("UnusedAssignment")
    public static byte[] upgradeDataVersion(byte[] data, String filePath) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(data);
        inputStream = new BOMInputStream(inputStream, false); // Exclude UTF8 Byte Order Mark. See: https://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
        
        if (!requiresUpgrade(inputStream, true)) {
            return data;
        }
        
        String battleScribeVersion = getBattleScribeVersion(inputStream, true);
        
        HashMap<String, ByteArrayInputStream> styleSheetMap = getStyleSheetMap();
        try {
            if (isCataloguePath(filePath)) {
                // 1.13b -> 1.15b
                if (battleScribeVersion.compareToIgnoreCase("1.15") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.CATALOGUE_1_15_XSL_FILE_PATH));
                    battleScribeVersion = "1.15";
                }
                // 1.15 -> 2.00
                if (battleScribeVersion.compareToIgnoreCase("2.00") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.CATALOGUE_2_00_XSL_FILE_PATH));
                    battleScribeVersion = "2.00";
                }
                // 2.00 -> 2.01
                if (battleScribeVersion.compareToIgnoreCase("2.01") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.CATALOGUE_2_01_XSL_FILE_PATH));
                    battleScribeVersion = "2.01";
                }
                // 2.01 -> 2.02
                if (battleScribeVersion.compareToIgnoreCase("2.02") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.CATALOGUE_2_02_XSL_FILE_PATH));
                    battleScribeVersion = "2.02";
                }
                // 2.02 -> 2.03
//                if (battleScribeVersion.compareToIgnoreCase("2.03") < 0) {
//                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.CATALOGUE_2_03_XSL_FILE_PATH));
//                    battleScribeVersion = "2.03";
//                }
            }
            else if (isGameSytstemPath(filePath)) {
                // 1.13b -> 1.15b
                if (battleScribeVersion.compareToIgnoreCase("1.15") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.GAME_SYSTEM_1_15_XSL_FILE_PATH));
                    battleScribeVersion = "1.15";
                }
                // 1.15 -> 2.00
                if (battleScribeVersion.compareToIgnoreCase("2.00") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.GAME_SYSTEM_2_00_XSL_FILE_PATH));
                    battleScribeVersion = "2.00";
                }
                // 2.00 -> 2.01
                if (battleScribeVersion.compareToIgnoreCase("2.01") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.GAME_SYSTEM_2_01_XSL_FILE_PATH));
                    battleScribeVersion = "2.01";
                }
                // 2.01 -> 2.02
                if (battleScribeVersion.compareToIgnoreCase("2.02") < 0) {
                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.GAME_SYSTEM_2_02_XSL_FILE_PATH));
                    battleScribeVersion = "2.02";
                }
                // 2.02 -> 2.03
//                if (battleScribeVersion.compareToIgnoreCase("2.03") < 0) {
//                    inputStream = transfromXml(inputStream, styleSheetMap.get(FileConstants.GAME_SYSTEM_2_03_XSL_FILE_PATH));
//                    battleScribeVersion = "2.03";
//                }
            }
        }
        finally {
            resetStreamQuietly(inputStream);
            for (ByteArrayInputStream styleSheet : styleSheetMap.values()) {
                resetStreamQuietly(styleSheet);
            }
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream);

        return outputStream.toByteArray();
    }
    
    public static boolean requiresUpgrade(InputStream inputStream, boolean resetInputStream) throws IOException {
        String battleScribeVersion = getBattleScribeVersion(inputStream, resetInputStream);
        
        if (Utils.isNullOrEmpty(battleScribeVersion)
                || battleScribeVersion.compareToIgnoreCase(DataConstants.MIN_DATA_FORMAT_VERSION) < 0) {
            
            throw new IOException("Data file is too old and is no longer supported (" + battleScribeVersion + ")");
        }
        
        return battleScribeVersion.compareToIgnoreCase(DataConstants.CURRENT_DATA_FORMAT_VERSION) < 0;
    }

    /**
     * Buffers internally.
     * 
     * @param inputStream
     * @param resetInputStream
     * @return 
     * @throws IOException 
     */
    public static String getBattleScribeVersion(InputStream inputStream, boolean resetInputStream) throws IOException {
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            InputStream inputStreamCopy = readPartialStream(inputStream, resetInputStream);
            saxParser.parse(inputStreamCopy, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    String battleScribeVersion = attributes.getValue(DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE);
                    throw new SAXParseCompleteException(battleScribeVersion);
                }
            });
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        catch (SAXParseCompleteException e) {
            return e.getMessage();
        }
        catch (Exception ex) {
            throw new IOException(ex);
        }
        return null;
    }
    
    private static HashMap<String, ByteArrayInputStream> styleSheetMapCache;
    private static HashMap<String, ByteArrayInputStream> getStyleSheetMap() {
        if (styleSheetMapCache == null) {
            styleSheetMapCache = new HashMap<>();
            
            try {
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.CATALOGUE_1_15_XSL_FILE_PATH);
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.GAME_SYSTEM_1_15_XSL_FILE_PATH);

                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.CATALOGUE_2_00_XSL_FILE_PATH);
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.GAME_SYSTEM_2_00_XSL_FILE_PATH);

                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.CATALOGUE_2_01_XSL_FILE_PATH);
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.GAME_SYSTEM_2_01_XSL_FILE_PATH);

                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.CATALOGUE_2_02_XSL_FILE_PATH);
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.GAME_SYSTEM_2_02_XSL_FILE_PATH);

                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.CATALOGUE_2_03_XSL_FILE_PATH);
                addStyleSheetMapEntry(styleSheetMapCache, FileConstants.GAME_SYSTEM_2_03_XSL_FILE_PATH);
            }
            catch (IOException e) {
                throw new RuntimeException(e); // We can't run the app without reading stylesheets. KILL IT WITH FIRE.
            }
        }
        return styleSheetMapCache;
    }
    
    private static void addStyleSheetMapEntry(HashMap<String, ByteArrayInputStream> styleSheetMap, String styleSheetPath) throws IOException {
        styleSheetMap.put(styleSheetPath, readResource(styleSheetPath));
    }
    
    public static ByteArrayInputStream readResource(String resourcePath) throws IOException {
        InputStream inputStream = new BufferedInputStream(Utils.class.getResourceAsStream(resourcePath));
        
        return readStreamToMemory(inputStream);
    }

    /**
     * Buffers internally.
     * Closes sourceStream (but not styleSheet).
     * 
     * @param sourceStream
     * @param styleSheet
     * @return
     * @throws IOException 
     */
    public static ByteArrayInputStream transfromXml(InputStream sourceStream, InputStream styleSheet) throws IOException {
        if (!sourceStream.markSupported() || !styleSheet.markSupported()) {
            throw new IllegalArgumentException();
        }
        
        try {
            TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(styleSheet));

            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(1024 * 100);
            transformer.transform(new StreamSource(sourceStream), new StreamResult(byteOutputStream));
            
            return new ByteArrayInputStream(byteOutputStream.toByteArray());
        }
        catch (TransformerConfigurationException e) {
            throw new IllegalStateException();
        }
        catch (TransformerException e) {
            throw new IOException(e);
        }
        finally {
            IOUtils.closeQuietly(sourceStream);
        }
    }
}

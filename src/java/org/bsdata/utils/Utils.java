
package org.bsdata.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bsdata.constants.DataConstants;


public class Utils {
    
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
}

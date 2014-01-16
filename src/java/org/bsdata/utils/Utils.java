
package org.bsdata.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

        if (!url.contains("://")) {
            url = "http://" + url;
        }
        url = url.trim().replace(" ", "%20");
        
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

    public static boolean isRosterFile(File file) {
        return isRosterPath(file.getName());
    }

    public static boolean isRosterPath(String path) {
        path = path.trim().toLowerCase();
        if (path.endsWith(DataConstants.ROSTER_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD)) {
            return true;
        }
        return false;
    }

    public static boolean isCatalogueFile(File file) {
        return isCataloguePath(file.getName());
    }

    public static boolean isCataloguePath(String path) {
        path = path.trim().toLowerCase();
        if (path.endsWith(DataConstants.CATALOGUE_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD)) {
            return true;
        }
        return false;
    }
    
    public static boolean isGameSytstemFile(File file) {
        return isGameSytstemPath(file.getName());
    }

    public static boolean isGameSytstemPath(String path) {
        path = path.trim().toLowerCase();
        if (path.endsWith(DataConstants.GAME_SYSTEM_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD)) {
            return true;
        }
        return false;
    }

    public static boolean isIndexFile(File file) {
        return isIndexPath(file.getName());
    }

    public static boolean isIndexPath(String path) {
        path = path.trim().toLowerCase();
        if (path.endsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.DEFAULT_INDEX_FILE_NAME)) {
            return true;
        }
        return false;
    }

    public static boolean isCompressedFile(File file) {
        return isCompressedPath(file.getName());
    }
    
    public static boolean isCompressedPath(String path) {
        path = path.trim().toLowerCase();
        if (path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                || path.endsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD)
                || path.endsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION)) {
            return true;
        }
        return false;
    }
    
    public static String getCompressedFileName(File file) {
        return getCompressedFileName(file);
    }
    
    public static String getCompressedFileName(String fileName) {
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
            return FilenameUtils.getName(fileName);
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
        
        return fileName;
    }
    
    public static String getUncompressedFileName(File file) {
        return getUncompressedFileName(file.getName());
    }
    
    public static String getUncompressedFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        if (!isCompressedPath(fileName)) {
            return FilenameUtils.getName(fileName);
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
     * @return 
     */
    public static ByteArrayInputStream decompressStream(InputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = getDecompressedInputStream(inputStream);
        return readStreamToMemory(zipInputStream);
    }
    
    /**
     * Buffers internally.
     * Closes inputStream.
     * 
     * @param zipEntryName
     * @param inputStream
     * @return
     * @throws IOException 
     */
    public static ByteArrayInputStream compressStream(String zipEntryName, InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = getCompressedOutputStream(zipEntryName, outputStream);

            IOUtils.copy(inputStream, zipOutputStream);
            zipOutputStream.finish();
            
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    public static ZipOutputStream getCompressedOutputStream(String zipEntryName, OutputStream outputStream) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        ZipEntry zipEntry = new ZipEntry(getUncompressedFileName(zipEntryName));
        zipOutputStream.putNextEntry(zipEntry);
        return zipOutputStream;
    }
    
    public static ZipInputStream getDecompressedInputStream(InputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        zipInputStream.getNextEntry();
        return zipInputStream;
    }
}

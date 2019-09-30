using dotnetCore.constants;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Runtime.Serialization.Formatters.Binary;
using System.Threading.Tasks;
using System.Xml;

namespace dotnetCore.Utilities
{
    public static class Utils
    {
        public static string CheckUrl(String url)
        {
            if (string.IsNullOrWhiteSpace(url))
            {
                return null;
            }

            url = url.Trim().Replace(" ", "%20");
            if (!url.Contains("://"))
            {
                url = "http://" + url;
            }

            if (url == "http://")
            {
                return null;
            }

            try
            {
                Uri uri = new Uri(url);
            }
            catch (Exception ex)
            {
                return null;
            }

            return url;
        }
        public static bool IsDataFilePath(string path)
        {
            return (IsRosterPath(path) || IsCataloguePath(path) || IsGameSytstemPath(path));
        }

        public static bool IsRosterPath(String path)
        {
            path = path.Trim().ToLower();
            return path.EndsWith(DataConstants.ROSTER_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD);
        }

        public static bool IsCataloguePath(String path)
        {
            path = path.Trim().ToLower();
            return path.EndsWith(DataConstants.CATALOGUE_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD);
        }

        public static bool IsGameSytstemPath(String path)
        {
            path = path.Trim().ToLower();
            return path.EndsWith(DataConstants.GAME_SYSTEM_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD);
        }

        public static bool IsCompressedPath(String path)
        {
            path = path.Trim().ToLower();
            return path.EndsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD)
                    || path.EndsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD)
                    || path.EndsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD)
                    || path.EndsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION);
        }

        public static bool IsIndexPath(String path)
        {
            path = path.Trim().ToLower();
            return path.EndsWith(DataConstants.INDEX_COMPRESSED_FILE_EXTENSION)
                    || path.EndsWith(DataConstants.DEFAULT_INDEX_FILE_NAME);
        }

        public static XmlDocument UpgradeDataVersion(XmlDocument xmlDocument, string filePath)
        {
            if (!RequiresUpgrade(xmlDocument))
            {
                return xmlDocument;
            }

            var battleScribeVersion = GetBattleScribeVersion(xmlDocument);


            return xmlDocument;
        }

        public static bool RequiresUpgrade(XmlDocument xmlDocument)
        {
            string battleScribeVersion = GetBattleScribeVersion(xmlDocument);

            if (string.IsNullOrWhiteSpace(battleScribeVersion) ||
                string.Compare(battleScribeVersion, DataConstants.MIN_DATA_FORMAT_VERSION) < 0)
            {
                throw new Exception($"Data file is too old and is no longer supported ({battleScribeVersion})");
            }

            return string.Compare(battleScribeVersion, DataConstants.CURRENT_DATA_FORMAT_VERSION) < 0;

        }

        public static string GetBattleScribeVersion(XmlDocument xmlDocument)
        {
            var battleScribeVersion = xmlDocument.DocumentElement.Attributes[DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE].Value;
            return battleScribeVersion;
        }

        public static byte[] GetByteArrayForZipArchiveEntry(ZipArchiveEntry zipArchiveEntry)
        {
            var stream = zipArchiveEntry.Open();
            byte[] bytes;
            using (var ms = new MemoryStream())
            {
                stream.CopyTo(ms);
                bytes = ms.ToArray();
            }

            return bytes;
        }

        public static string GetCompressedFileName(String fileName)
        {
            if (string.IsNullOrWhiteSpace(fileName))
            {
                return null;
            }

            fileName = Path.GetFileName(fileName);

            // LEGACY FILE NAMES //
            if (fileName.EndsWith(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD))
            {
                fileName = fileName.Replace(DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION);
            }
            else if (fileName.EndsWith(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD))
            {
                fileName = fileName.Replace(DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION);
            }
            else if (fileName.EndsWith(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD))
            {
                fileName = fileName.Replace(DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION_OLD, DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION);
            }

            if (IsCompressedPath(fileName))
            {
                return fileName;
            }

            if (IsGameSytstemPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.GAME_SYSTEM_COMPRESSED_FILE_EXTENSION;
            }
            else if (IsCataloguePath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.CATALOGUE_COMPRESSED_FILE_EXTENSION;
            }
            else if (IsRosterPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.ROSTER_COMPRESSED_FILE_EXTENSION;
            }
            else if (IsIndexPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.INDEX_COMPRESSED_FILE_EXTENSION;
            }

            return fileName;

        }

        public static String GetUncompressedFileName(String fileName)
        {
            if (string.IsNullOrWhiteSpace(fileName))
            {
                return null;
            }

            fileName = Path.GetFileName(fileName);

            if (!IsCompressedPath(fileName))
            {
                return fileName;
            }

            if (IsGameSytstemPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.GAME_SYSTEM_FILE_EXTENSION;
            }
            else if (IsCataloguePath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.CATALOGUE_FILE_EXTENSION;
            }
            else if (IsRosterPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.ROSTER_FILE_EXTENSION;
            }
            else if (IsIndexPath(fileName))
            {
                fileName = Path.GetFileNameWithoutExtension(fileName) + DataConstants.XML_FILE_EXTENSION;
            }

            return fileName;
        }

        public static byte[] CompressData(string zipEntryName, byte[] data)
        {
            if (IsCompressedPath(zipEntryName))
            {
                throw new ArgumentException("Zip entry name must have an uncompressed file extention");
            }

            var inputStream = new MemoryStream(data);
            byte[] compressedBytes;

            using (var outStream = new MemoryStream())
            {
                using (var archive = new ZipArchive(outStream, ZipArchiveMode.Create, true))
                {
                    var fileName = GetUncompressedFileName(zipEntryName);

                    var fileInArchive = archive.CreateEntry(fileName, CompressionLevel.Optimal);
                    using (var entryStream = fileInArchive.Open())
                    using (var fileToCompressStream = new MemoryStream(data))
                    {
                        fileToCompressStream.CopyTo(entryStream);
                    }
                }
                compressedBytes = outStream.ToArray();
            }

            return compressedBytes;
        }

        public static byte[] DecompressData(byte[] data)
        {
            using (var memoryStream = new MemoryStream(data))
            {
                using (var archive = new ZipArchive(memoryStream))
                {
                    foreach (ZipArchiveEntry entry in archive.Entries)
                    {
                        using (var entryStream = entry.Open())
                        {
                            using (var reader = new BinaryReader(entryStream))
                            {
                                return reader.ReadBytes((int)entry.Length);
                            }
                        }
                    }
                }
            }
            return null; // To quiet my compiler
        }

        // Convert an object to a byte array
        public static byte[] ObjectToByteArray(Object obj)
        {
            BinaryFormatter bf = new BinaryFormatter();
            using (var ms = new MemoryStream())
            {
                bf.Serialize(ms, obj);
                return ms.ToArray();
            }
        }

        // Convert a byte array to an Object
        public static Object ByteArrayToObject(byte[] arrBytes)
        {
            using (var memStream = new MemoryStream())
            {
                var binForm = new BinaryFormatter();
                memStream.Write(arrBytes, 0, arrBytes.Length);
                memStream.Seek(0, SeekOrigin.Begin);
                var obj = binForm.Deserialize(memStream);
                return obj;
            }
        }
    }
}

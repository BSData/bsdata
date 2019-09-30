using dotnetCore.constants;
using dotnetCore.Models;
using dotnetCore.Utilities;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Xml;

namespace dotnetCore.Services
{
    public class IndexerService : IIndexerService
    {

        public Dictionary<string, DataFile> CreateRepositoryData(
            string repositoryName,
            string baseUrl,
            List<string> repositoryUrls,
            Dictionary<string, byte[]> fileDatas)
        {
            var indexUrl = Utils.CheckUrl($"{baseUrl}/{repositoryName}/{DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME}");
            var dataIndex = new DataIndex(repositoryName, indexUrl, repositoryUrls);

            var repositoryData = new Dictionary<string, DataFile>();
            var fileIds = new List<string>();

            foreach (var filePath in fileDatas.Keys)
            {
                if (!Utils.IsDataFilePath(filePath))
                {
                    // Skip any files that aren't data files
                    continue;
                }

                if (Utils.IsCompressedPath(filePath))
                {
                    // We should only get uncompressed data at this point
                    throw new ArgumentException($"Data file {filePath} is already compressed.");
                }

                var fileData = fileDatas.GetValueOrDefault(filePath);
                var inputStream = new MemoryStream(fileData);

                // Make sure we have just the filename, without the path
                String fileName = Path.GetFileName(filePath);

                var dataFile = new DataFile();
                if (Utils.IsGameSytstemPath(fileName))
                {
                    dataFile = ReadGameSystem(inputStream);
                }
                else if (Utils.IsCataloguePath(fileName))
                {
                    dataFile = ReadCatalogue(inputStream);
                }
                else if (Utils.IsRosterPath(fileName))
                {
                    dataFile = ReadRoster(inputStream);
                }
                else
                {
                    continue;
                }

                if (fileIds.Contains(dataFile.Id))
                {
                    continue; // Skip if we've already come accross this ID for this repo
                }

                fileIds.Add(dataFile.Id);

                // Compress the file data and set it on the DataFile
                fileData = Utils.CompressData(fileName, fileData);
                dataFile.Data = fileData.ToArray();

                //// Create a DataIndexEntry using compressed file name
                fileName = Utils.GetCompressedFileName(fileName);
                var dataIndexEntry = new DataIndexEntry(fileName, dataFile);

                //// Add our data file and index entry
                repositoryData.Add(fileName, dataFile);
                dataIndex.DataIndexEntries.Add(dataIndexEntry);
            }

            // Compress the index file data and set it on the DataIndex
            byte[] indexData = Utils.ObjectToByteArray(dataIndex);            
            indexData = Utils.CompressData(DataConstants.DEFAULT_INDEX_FILE_NAME, indexData);
            dataIndex.Data = indexData;

            // Add the DataIndex to the hashmap
            repositoryData.Add(DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME, dataIndex);


            return repositoryData;
        }

        private Catalogue ReadCatalogue(MemoryStream inputStream)
        {
            try
            {
                var catalogue = new Catalogue();

                var xmlDocument = new XmlDocument();
                xmlDocument.Load(inputStream);

                if (string.Equals(xmlDocument.DocumentElement.Name, DataConstants.CATALOGUE_TAG, StringComparison.OrdinalIgnoreCase))
                {
                    catalogue.Id = xmlDocument.DocumentElement.Attributes[DataConstants.ID_ATTRIBUTE].Value;
                    catalogue.GameSystemId = xmlDocument.DocumentElement.Attributes[DataConstants.GAME_SYSTEM_ID_ATTRIBUTE].Value;
                    catalogue.BattleScribeVersion = xmlDocument.DocumentElement.Attributes[DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE].Value;
                    catalogue.Revision = int.Parse(xmlDocument.DocumentElement.Attributes[DataConstants.REVISION_ATTRIBUTE].Value);
                    catalogue.Name = xmlDocument.DocumentElement.Attributes[DataConstants.NAME_ATTRIBUTE].Value;
                    catalogue.AuthorName = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_NAME_ATTRIBUTE].Value;
                    catalogue.AuthorContact = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_CONTACT_ATTRIBUTE].Value;
                    catalogue.AuthorUrl = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_URL_ATTRIBUTE].Value;

                }

                return catalogue;
            }
            catch (Exception ex)
            {
                throw new XmlException("Invalid catalogue XML", ex);
            }
        }

        private GameSystem ReadGameSystem(MemoryStream inputStream)
        {
            try
            {
                var gameSystem = new GameSystem();

                var xmlDocument = new XmlDocument();
                xmlDocument.Load(inputStream);

                if (string.Equals(xmlDocument.DocumentElement.Name, DataConstants.GAME_SYSTEM_TAG, StringComparison.OrdinalIgnoreCase))
                {

                    gameSystem.Id = xmlDocument.DocumentElement.Attributes[DataConstants.ID_ATTRIBUTE].Value;
                    gameSystem.BattleScribeVersion = xmlDocument.DocumentElement.Attributes[DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE].Value;
                    gameSystem.Revision = int.Parse(xmlDocument.DocumentElement.Attributes[DataConstants.REVISION_ATTRIBUTE].Value);
                    gameSystem.Name = xmlDocument.DocumentElement.Attributes[DataConstants.NAME_ATTRIBUTE].Value;
                    gameSystem.AuthorName = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_NAME_ATTRIBUTE].Value;
                    gameSystem.AuthorContact = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_CONTACT_ATTRIBUTE].Value;
                    gameSystem.AuthorUrl = xmlDocument.DocumentElement.Attributes[DataConstants.AUTHOR_URL_ATTRIBUTE].Value;

                }

                return gameSystem;
            } catch(Exception ex)
            {
                throw new XmlException("Invalid catalogue XML", ex);
            }
        }

        private Roster ReadRoster(MemoryStream inputStream)
        {
            try
            {
                var roster = new Roster();

                var xmlDocument = new XmlDocument();
                xmlDocument.Load(inputStream);

                if (string.Equals(xmlDocument.DocumentElement.Name, DataConstants.CATALOGUE_TAG, StringComparison.OrdinalIgnoreCase))
                {
                    roster.BattleScribeVersion = xmlDocument.DocumentElement.Attributes[DataConstants.BATTLESCRIBE_VERSION_ATTRIBUTE].Value;
                    roster.Description = xmlDocument.DocumentElement.Attributes[DataConstants.DESCRIPTION_ATTRIBUTE].Value;
                    roster.Name = xmlDocument.DocumentElement.Attributes[DataConstants.NAME_ATTRIBUTE].Value;
                    roster.Points = int.Parse(xmlDocument.DocumentElement.Attributes[DataConstants.POINTS_ATTRIBUTE].Value);
                    roster.PointsLimit = int.Parse(xmlDocument.DocumentElement.Attributes[DataConstants.POINTS_LIMIT_ATTRIBUTE].Value);
                    roster.GameSystemId = xmlDocument.DocumentElement.Attributes[DataConstants.GAME_SYSTEM_ID_ATTRIBUTE].Value;
                    
                    if (xmlDocument.DocumentElement.HasAttribute(DataConstants.GAME_SYSTEM_NAME_ATTRIBUTE))
                    {
                        roster.GameSystemName = xmlDocument.DocumentElement.Attributes[DataConstants.GAME_SYSTEM_NAME_ATTRIBUTE].Value;
                    }

                    if (xmlDocument.DocumentElement.HasAttribute(DataConstants.GAME_SYSTEM_REVISION_ATTRIBUTE))
                    {
                        roster.GameSystemRevision = int.Parse(xmlDocument.DocumentElement.Attributes[DataConstants.GAME_SYSTEM_REVISION_ATTRIBUTE].Value);
                    }
                }

                return roster;
            }
            catch (Exception ex)
            {
                throw new XmlException("Invalid catalogue XML", ex);
            }
        }
    }
}

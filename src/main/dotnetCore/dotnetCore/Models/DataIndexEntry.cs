using dotnetCore.constants;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Models
{
    [Serializable]
    public class DataIndexEntry
    {
        public string FilePath { get; set; }
        public string DataType { get; set; }
        public string DataId { get; set; }
        public string DataName { get; set; }
        public string DataBattleScribeVersion { get; set; }
        public int DataRevision { get; set; }
        public DateTime LastModified { get; set; }

        public DataIndexEntry(string filePath, DataFile dataFile)
        {
            FilePath = filePath;

            if (dataFile.GetType() == typeof(GameSystem))
            {
                var gameSystem = (GameSystem)dataFile;

                DataType = DataConstants.DataType.GAME_SYSTEM;
                DataId = gameSystem.Id;
                DataName = gameSystem.Name;
                DataBattleScribeVersion = gameSystem.BattleScribeVersion;
                DataRevision = gameSystem.Revision;
            }
            else if (dataFile.GetType() == typeof(Catalogue))
            {
                var catalogue = (Catalogue)dataFile;

                DataType = DataConstants.DataType.CATALOGUE;
                DataId = catalogue.Id;
                DataName = catalogue.Name;
                DataBattleScribeVersion = catalogue.BattleScribeVersion;
                DataRevision = catalogue.Revision;
            }
            else if (dataFile.GetType() == typeof(Roster))
            {
                var roster = (Roster)dataFile;

                DataType = DataConstants.DataType.ROSTER;
                DataId = roster.Id;
                DataName = roster.Name;
                DataBattleScribeVersion = roster.BattleScribeVersion;
                DataRevision = roster.Revision;
            }
            else
            {
                throw new ArgumentException("Not a data file");
            }
        }
    }
}

using dotnetCore.Utilities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Models
{
    [Serializable]
    public class DataIndex : DataFile
    {
        public string BattleScribeVersion { get; set; }
        public string Name { get; set; }
        public string IndexUrl { get; set; }
        public List<string> RepositoryUrls { get; set; }
        public List<DataIndexEntry> DataIndexEntries { get; set; }

        public DataIndex(string name, string indexUrl)
        {
            BattleScribeVersion = "1.13b";
            Name = name;

            indexUrl = Utils.CheckUrl(indexUrl);
            if(!string.IsNullOrWhiteSpace(indexUrl))
            {
                IndexUrl = indexUrl;
            }

            DataIndexEntries = new List<DataIndexEntry>();
            RepositoryUrls = new List<string>();
        }

        public DataIndex(string name, string indexUrl, List<string> repositoryUrls) : this(name, indexUrl)
        {

            if (repositoryUrls != null)
            {
                foreach (var url in repositoryUrls)
                {
                    var uri = Utils.CheckUrl(url);
                    if (string.IsNullOrWhiteSpace(uri) || RepositoryUrls.Contains(uri))
                    {
                        continue;
                    }

                    RepositoryUrls.Add(uri);
                }
            }
        }

    }
}

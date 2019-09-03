using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Models
{
    [Serializable]
    public class DataFile
    {
        public byte[] Data { get; set; }            
        public string Id { get; set; }
        public string Name { get; set; }
        public int Revision { get; set; }
        public string BattleScribeVersion { get; set; }
        public string AuthorName { get; set; }
        public string AuthorContact { get; set; }
        public string AuthorUrl { get; set; }

    }
}

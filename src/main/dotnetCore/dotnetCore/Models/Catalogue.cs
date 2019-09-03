using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Models
{
    [Serializable]
    public class Catalogue : DataFile
    {
        public string GameSystemId { get; set; }
        public int GameSystemRevision { get; set; }
    }
}

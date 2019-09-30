using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Models
{
    public class Roster : DataFile
    {
        public double Points { get; set; }
        public double PointsLimit { get; set; }
        public string Description { get; set; }
        public string GameSystemId { get; set; }
        public string GameSystemName { get; set; }
        public int GameSystemRevision { get; set; }
    }
}

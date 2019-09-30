using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.ViewModel
{
    public class RepositoryVm : ResponseVm
    {
        public string Name { get; set; }
        public string Description { get; set; }
        public string BattleScribeVersion { get; set; }
    
        public string Version { get; set; }
        public string LastUpdated { get; set; }
        public string LastUpdateDescription { get; set; }
    
        public string IndexUrl { get; set; }
        public string RepositoryUrl { get; set; }
        public string GithubUrl { get; set; }
        public string FeedUrl { get; set; }
        public string BugTrackerUrl { get; set; }
        public string ReportBugUrl { get; set; }

        public List<RepositoryFileVm> RepositoryFiles { get; set; } = new List<RepositoryFileVm>();
    }
}

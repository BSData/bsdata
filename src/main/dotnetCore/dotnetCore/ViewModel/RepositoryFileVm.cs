using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.ViewModel
{
    public class RepositoryFileVm : ResponseVm
    {
        public string  Id { get; set; }
        public string  Name { get; set; }
        public string  Type { get; set; }
        public int Revision { get; set; }
        public string  BattleScribeVersion { get; set; }

        public string  FileUrl { get; set; }
        public string  GithubUrl { get; set; }
        public string  BugTrackerUrl { get; set; }
        public string  ReportBugUrl { get; set; }
        public string  AuthorName { get; set; }
        public string  AuthorContact { get; set; }
        public string  AuthorUrl { get; set; }
    }
}

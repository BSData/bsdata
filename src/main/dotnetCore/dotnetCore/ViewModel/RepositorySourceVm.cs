using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.ViewModel
{
    public class RepositorySourceVm : ResponseVm
    {
        public string Name { get; set; }
        public string Description { get; set; }
        public string BattleScribeVersion { get; set; }
        public string RepositorySourceUrl { get; set; }
        public string WebsiteUrl { get; set; }
        public string GithubUrl { get; set; }
        public string DiscordUrl { get; set; }
        public string FeedUrl { get; set; }
        public string TwitterUrl { get; set; }
        public string FacebookUrl { get; set; }

        public List<RepositoryVm> Repositories { get; set; } = new List<RepositoryVm>();
    }
}

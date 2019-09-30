namespace dotnetCore.Services
{
    public class AppSettings
    {
        public GithubSettings GitHub { get; set; } = new GithubSettings();
        public SiteSettings Site { get; set; } = new SiteSettings();
    }

    public class GithubSettings
    {
        public string Username { get; set; }
        public string Token { get; set; }
        public string Email { get; set; }

        public string Organisation { get; set; }
        public string MasterBranch { get; set; }
        public string BsDataRepo { get; set; }
    }
    public class SiteSettings
    {
        public string Name { get; set; }
        public string Description { get; set; }

        public string WebsiteUrl { get; set; }
        public string GitHubUrl { get; set; }
        public string DiscordUrl { get; set; }
        public string TwitterUrl { get; set; }
        public string FacebookUrl { get; set; }
    }
}
using dotnetCore.constants;
using dotnetCore.Constants;
using dotnetCore.Utilities;
using dotnetCore.ViewModel;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Octokit;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace dotnetCore.Services
{
    public class GitHubService : IGitHubService
    {

        private static bool ReposRequiresUpdate = true;

        private int MAX_FEED_ENTRIES = 5;
        private const int REPO_CACHE_EXPIRY_MINS = 12 * 60;
        
        private readonly string branchDateFormat = "yyMMddHHmmssSSS";
        private readonly string longDateFormat = "yyyy-MM-ddTHH:mm:ss.fffzzz";

        private readonly ILogger _logger;
        private readonly IMemoryCache _cache;
        private readonly AppSettings _appSettings;

        private readonly object reposUpdateLock = new object();

        public GitHubService(
            ILogger<GitHubService> logger, 
            IMemoryCache memoryCache,
            AppSettings appSettings)
        {
            _logger = logger;
            _cache = memoryCache;
            _appSettings = appSettings;
        }

        public async Task<RepositorySourceVm> GetRepos(string baseUrl)
        {
            var orgRepositories = await GetRepositories();

            var isDev = baseUrl.ToLower().Contains("localhost") || baseUrl.ToLower().Contains("battlescribedatatest");

            var repositoryVms = new List<RepositoryVm>();

            foreach (var repository in orgRepositories)
            {
                var bsdataRepoName = _appSettings.GitHub.BsDataRepo;

                // Don't include the repo for the data site
                if(repository.Name == bsdataRepoName)
                {
                    continue;
                }

                // Hack to prevent test repo showing up on the live site
                if (!isDev && repository.Name.ToLower() == "test")
                {
                    continue;
                }

                try
                {
                    var latestRelease = await GetLatestRelease(repository);
                    if (latestRelease == null)
                    {
                        continue;
                    }

                    var repositoryVm = CreateRepositoryVm(baseUrl, repository, latestRelease);
                    repositoryVms.Add(repositoryVm);
                }
                catch (Exception ex)
                {
                    _logger.LogError($"IOException getting latest release for {repository.Name}", ex);
                }

            }

            var repositorySourceVm = new RepositorySourceVm();

            repositorySourceVm.Repositories = repositoryVms.OrderBy(x => x.Description).ToList();
            repositorySourceVm.Name = _appSettings.Site.Name;
            repositorySourceVm.Description = _appSettings.Site.Description;
            repositorySourceVm.BattleScribeVersion = DataConstants.CURRENT_DATA_FORMAT_VERSION;

            repositorySourceVm.WebsiteUrl = _appSettings.Site.WebsiteUrl;

            repositorySourceVm.RepositorySourceUrl = baseUrl;
            repositorySourceVm.FeedUrl = $"{baseUrl}/feeds/all.atom";
            repositorySourceVm.GithubUrl = _appSettings.Site.GitHubUrl;
            repositorySourceVm.DiscordUrl = _appSettings.Site.DiscordUrl;
            repositorySourceVm.TwitterUrl = _appSettings.Site.TwitterUrl;
            repositorySourceVm.FacebookUrl = _appSettings.Site.FacebookUrl;

            return repositorySourceVm;
        }


        #region Private Functions

        private async Task<Release> GetLatestRelease(Repository repository)
        {
            if (repository == null)
            {
                return null;
            }

            var releases = await GetReleases(repository);
            if(!releases.Any())
            {
                return null;
            }

            return releases[0];
        }
        
        private async Task<IReadOnlyList<Release>> GetReleases(Repository repository)
        {
            if (repository == null)
            {
                return new List<Release>();
            }

            var repositoryName = repository.Name;

            var cachedReleases = _cache.Get<Dictionary<string, IReadOnlyList<Release>>>(CacheKeys.RepositoryReleases);

            var releases = cachedReleases.GetValueOrDefault(repository.Name);

            if(releases == null)
            {
                await RefreshRepositoriesAsync();
                cachedReleases = _cache.Get<Dictionary<string, IReadOnlyList<Release>>>(CacheKeys.RepositoryReleases);

                if (cachedReleases.ContainsKey(repository.Name))
                {
                    releases = cachedReleases.GetValueOrDefault(repository.Name);
                } else
                {
                    releases = new List<Release>();
                }

            }

            return releases;
        }

        private async Task<IReadOnlyList<Repository>> GetRepositories()
        {

            if (!_cache.TryGetValue(CacheKeys.Repositories, out IReadOnlyList<Repository> repositories))
            {
                repositories = await RefreshRepositoriesAsync();
                return repositories;
            }

            return repositories;
        }

        public async Task<IReadOnlyList<Repository>> RefreshRepositoriesAsync()
        {
            //lock(reposUpdateLock)
            //{
            
            // Get repos and releases and store them in the cache.
            var gitHubRepositories = await GetGitHubRepositories();
            List<Repository> tempRepositories = new List<Repository>();
            Dictionary<string, IReadOnlyList<Release>> tempRepositoryReleases = new Dictionary<string, IReadOnlyList<Release>>();

            foreach (var repository in gitHubRepositories)
            {
                IReadOnlyList<Release> gitHubReleases = new List<Release>();

                try
                {
                    gitHubReleases = await GetGitHubRelease(repository);
                }
                catch (Exception ex)
                {
                    _logger.LogError($"Failed to update data for repository {repository.Name}", ex);
                    continue;
                }

                tempRepositories.Add(repository);
                tempRepositoryReleases.Add(repository.Name, gitHubReleases);
            }

            var cacheEntryOptions = new MemoryCacheEntryOptions()
                .SetPriority(CacheItemPriority.NeverRemove);
                //.SetAbsoluteExpiration(new TimeSpan(0, REPO_CACHE_EXPIRY_MINS, 0));

            _cache.Set(CacheKeys.Repositories, tempRepositories, cacheEntryOptions);
            _cache.Set(CacheKeys.RepositoryReleases, tempRepositoryReleases, cacheEntryOptions);

            _cache.Set(CacheKeys.NextReposUpdateDate, DateTime.UtcNow.AddMinutes(REPO_CACHE_EXPIRY_MINS));

            return gitHubRepositories;
            //}
        }

        private async Task<IReadOnlyList<Repository>> GetGitHubRepositories()
        {
            _logger.LogInformation("Getting repositories from GitHub");

            var organizationName = _appSettings.GitHub.Organisation;
            var client = GetGitHubClient();

            var repos = await client.Repository.GetAllForOrg(organizationName);
            return repos;
        }

        public async Task<IReadOnlyList<Release>> GetGitHubRelease(Repository repository)
        {
            _logger.LogInformation($"Getting releases from GitHub for {repository.Name}");

            var client = GetGitHubClient();
            var releases = await client.Repository.Release.GetAll(repository.Id, new ApiOptions
            {
                StartPage = 1,
                PageCount = 1,
                PageSize = 5
            });

            return releases
                .Where(x => x.Draft == false)
                .OrderByDescending(x => x.PublishedAt)
                .ToList();
        }

        private GitHubClient GetGitHubClient() 
        {
            var client = new GitHubClient(new ProductHeaderValue(_appSettings.GitHub.Username));
            var tokenAuth = new Credentials(_appSettings.GitHub.Token); 
            client.Credentials = tokenAuth;

            return client;
        }

        public RepositoryVm CreateRepositoryVm(string baseUrl, Repository repository, Release latestRelease)
        {
            var repositoryVm = new RepositoryVm();
            repositoryVm.Name = repository.Name;
            repositoryVm.Description = repository.Description;
            repositoryVm.BattleScribeVersion = DataConstants.CURRENT_DATA_FORMAT_VERSION;

            if (latestRelease != null)
            {
                repositoryVm.Version = latestRelease.TagName;
                repositoryVm.LastUpdated = latestRelease.PublishedAt.Value.ToString(longDateFormat);
                repositoryVm.LastUpdateDescription = latestRelease.Name;
            }

            repositoryVm.RepositoryUrl = Utils.CheckUrl($"{baseUrl}/{repository.Name}");
            repositoryVm.IndexUrl = Utils.CheckUrl($"{baseUrl}/{repository.Name}/{DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME}");
            repositoryVm.GithubUrl = repository.HtmlUrl;
            repositoryVm.BugTrackerUrl = $"{repository.HtmlUrl}/issues";
            repositoryVm.ReportBugUrl = getFeedHref(baseUrl, repository.Name);
            repositoryVm.FeedUrl = Utils.CheckUrl($"{baseUrl}/feeds/{repository.Name}.atom");

            return repositoryVm;
        }

        private String getFeedHref(String baseUrl, String repositoryName)
        {
            if (repositoryName == null || repositoryName == WebConstants.ALL_REPO_FEEDS)
            {
                return Utils.CheckUrl(baseUrl.Replace(WebConstants.REPO_SERVICE_PATH, "#/repos"));
            }
            else
            {
                return Utils.CheckUrl(baseUrl.Replace(WebConstants.REPO_SERVICE_PATH, "#/repo/" + repositoryName));
            }
        }

        #endregion
    }
}

using dotnetCore.constants;
using dotnetCore.Constants;
using dotnetCore.Models;
using dotnetCore.Utilities;
using dotnetCore.ViewModel;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Octokit;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Net.Http;
using System.ServiceModel.Syndication;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Xml;

namespace dotnetCore.Services
{
    public class GitHubService : IGitHubService
    {
        private static HttpClient Client { get; } = new HttpClient();

        private static bool ReposRequiresUpdate = true;

        private int MAX_FEED_ENTRIES = 5;
        private const int REPO_CACHE_EXPIRY_MINS = 12 * 60;
        
        private readonly string branchDateFormat = "yyMMddHHmmssSSS";
        private readonly string longDateFormat = "yyyy-MM-ddTHH:mm:ss.fffzzz";

        private readonly ILogger _logger;
        private readonly IMemoryCache _cache;
        private readonly IIndexerService _indexerService;
        private readonly AppSettings _appSettings;

        private readonly object reposUpdateLock = new object();

        public GitHubService(
            ILogger<GitHubService> logger, 
            IMemoryCache memoryCache,
            AppSettings appSettings,
            IIndexerService indexerService)
        {
            _logger = logger;
            _cache = memoryCache;
            _appSettings = appSettings;
            _indexerService = indexerService;
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

        public async Task<RepositoryVm> GetRepoFiles(string baseUrl, string repositoryName)
        {
            var repository = await GetRepository(repositoryName);
            var latestRelease = await GetLatestRelease(repository);

            if (repository == null || latestRelease == null)
            {
                return null;
            }

            var repositoryVm = CreateRepositoryVm(baseUrl, repository, latestRelease);

            var repoFileData = await GetRepoFileData(baseUrl, repositoryName);
            var repositoryFileVms = new List<RepositoryFileVm>();

            foreach (var fileName in repoFileData.Keys)
            {
                if (!Utils.IsDataFilePath(fileName))
                {
                    continue;
                }

                var dataFile = repoFileData[fileName];
                var repositoryFile = new RepositoryFileVm();

                repositoryFile.Id = dataFile.Id;
                repositoryFile.Name = dataFile.Name;
                repositoryFile.Revision = dataFile.Revision;
                repositoryFile.BattleScribeVersion = dataFile.BattleScribeVersion;

                if (Utils.IsCataloguePath(fileName))
                {
                    repositoryFile.Type = DataConstants.DataType.CATALOGUE;
                }
                else if (Utils.IsGameSytstemPath(fileName))
                {
                    repositoryFile.Type = DataConstants.DataType.GAME_SYSTEM;
                }
                else if (Utils.IsRosterPath(fileName))
                {
                    repositoryFile.Type = DataConstants.DataType.ROSTER;
                }

                repositoryFile.FileUrl = Utils.CheckUrl(baseUrl + "/" + repository.Name + "/" + fileName);

                repositoryFile.ReportBugUrl = repositoryVm.ReportBugUrl;
                repositoryFile.BugTrackerUrl = repositoryVm.BugTrackerUrl;

                var uncompressedFileName = Utils.GetUncompressedFileName(fileName);
                repositoryFile.GithubUrl = Utils.CheckUrl(repositoryVm.GithubUrl + "/blob/master/" + uncompressedFileName);

                repositoryFile.AuthorName = dataFile.AuthorName;
                repositoryFile.AuthorContact = dataFile.AuthorContact;
                repositoryFile.AuthorUrl = dataFile.AuthorUrl;

                repositoryFileVms.Add(repositoryFile);
            }

            repositoryFileVms.Sort(
                delegate (RepositoryFileVm c1, RepositoryFileVm c2)
                {
                    if(c1.Type.ToLower() == DataConstants.DataType.CATALOGUE && 
                        c2.Type.ToLower() == DataConstants.DataType.GAME_SYSTEM)
                    {
                        return 1;
                    } else if(c1.Type.ToLower() == DataConstants.DataType.GAME_SYSTEM &&
                               c2.Type.ToLower() == DataConstants.DataType.CATALOGUE)
                    {
                        return -1;
                    }

                    return c1.Name.CompareTo(c2.Name);
                });

            repositoryVm.RepositoryFiles = repositoryFileVms;
            return repositoryVm;

        }
        
        #region Private Functions

        private async Task<Dictionary<string, DataFile>> GetRepoFileData(string baseUrl, string repositoryName)
        {
            var repository = await GetRepository(repositoryName);
            var latestRelease = await GetLatestRelease(repository);
            
            if (repository == null || latestRelease == null)
            {
                return new Dictionary<string, DataFile>();
            }

            var fileData = _cache.Get<Dictionary<string, DataFile>>(CacheKeys.RepositoryFiles + ":" + repositoryName);

            if(fileData == null)
            {
                await RefreshReleaseData(baseUrl, repository, latestRelease);
                if (!_cache.TryGetValue(CacheKeys.RepositoryFiles + ":" + repositoryName, out fileData))
                {
                    fileData = new Dictionary<string, DataFile>();
                }
            } else if(RequiresDataRefresh(repository,latestRelease))
            {
            // Todo: Fix stuff

            }

            return fileData;
        }

        private bool RequiresDataRefresh(Repository repository, Release latestRelease)
        {
            var repositoryName = repository.Name;

            // Todo: Lock

            if (latestRelease == null)
            {
                _cache.Set(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName, false);
                return false;
            }

            if(
                !_cache.TryGetValue(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName,out var dataRequiresUpdateFlags)
                || !_cache.TryGetValue(CacheKeys.RepositoryReleaseDates + ":" + repositoryName, out var repositoryReleaseDates)
                || !_cache.TryGetValue(CacheKeys.RepositoryFiles + ":" + repositoryName, out var repositoryFiles)
                || !_cache.TryGetValue(CacheKeys.RepositoryFeedEntries + ":" + repositoryName, out var repositoryFeedEntries)
                )
            {
                _cache.Set(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName, true);
                return true;
            }

            if (_cache.Get<bool>(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName) == true)
            {
                return true;
            }

            if (latestRelease.PublishedAt > _cache.Get<DateTimeOffset>(CacheKeys.RepositoryReleaseDates + ":" + repositoryName))
            {
                _cache.Set(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName, true);
                return true;
            }

            return false;

        }

        private async Task RefreshReleaseData(String baseUrl, Repository repository, Release latestRelease)
        {
            //Todo: Add Locks etc.
            var repositoryName = repository.Name;

            var dataFiles = await DownloadReleaseFromGithub(repository, latestRelease);
            var repositoryData = _indexerService.CreateRepositoryData(repositoryName, baseUrl, null, dataFiles);

            var releaseFeedEntries = await GetReleaseFeedEntries(baseUrl, repository);

            _cache.Set(CacheKeys.RepositoryFiles + ":" + repositoryName, repositoryData);
            _cache.Set(CacheKeys.RepositoryFeedEntries + ":" + repositoryName, releaseFeedEntries);
            _cache.Set(CacheKeys.RepositoryReleaseDates + ":" + repositoryName, latestRelease.PublishedAt);
            _cache.Set(CacheKeys.DataRequiresUpdateFlags + ":" + repositoryName, false);
            
        }

        private async Task<List<SyndicationItem>> GetReleaseFeedEntries(string baseUrl, Repository repository)
        {
            _logger.LogInformation($"Creating feed entries for {repository.Name}");

            var entries = new List<SyndicationItem>();

            var releases = await GetReleases(repository);
            foreach (var release in releases)
            {
                var entry = new SyndicationItem();

                entry.Id = release.HtmlUrl;
                entry.Title = new TextSyndicationContent(repository.Description + ": " + release.Name);
                entry.PublishDate = release.PublishedAt.Value;
                entry.LastUpdatedTime = release.PublishedAt.Value;

                var link = new SyndicationLink()
                {
                    MediaType = DataConstants.HTML_MIME_TYPE,
                    Uri = new Uri(getFeedHref(baseUrl, repository.Name))
                };
                entry.Links.Add(link);

                var contentBuffer = new StringBuilder();
                if(!string.IsNullOrWhiteSpace(release.Body))
                {
                    contentBuffer.Append("<p>");
                    contentBuffer.Append(release.Body);
                    contentBuffer.Append("</p>");
                }
                contentBuffer.Append("<p><a href=\"").Append(link.Uri).Append("\">Source repository details</a>");
                contentBuffer.Append("<br><a href=\"").Append(release.HtmlUrl).Append("\">GitHub release details</a></p>");

                var description = new TextSyndicationContent(contentBuffer.ToString(), TextSyndicationContentKind.Html);

                entry.Summary = description;

                entries.Add(entry);

                if(entries.Count >= MAX_FEED_ENTRIES)
                {
                    break;
                }
            }

            return entries;
        }

        private async Task<Dictionary<string, byte[]>> DownloadReleaseFromGithub(Repository repository, Release release)
        {
            _logger.LogInformation($"Downloading data from GitHub for {repository.Name}");

            var githubFiles = new Dictionary<string, byte[]>();

            var zipUrl = repository.HtmlUrl;
            if (!zipUrl.EndsWith("/"))
            {
                zipUrl += "/";
            }

            zipUrl += "archive/" + release.TagName + DataConstants.ZIP_FILE_EXTENSION;

            var stream = await Client.GetStreamAsync(zipUrl);
            var zip = new ZipArchive(stream);
            
            foreach (var file in zip.Entries)
            {
                if(!Utils.IsDataFilePath(file.Name))
                {
                    continue;
                }
                if (Utils.IsCompressedPath(file.Name))
                {
                    try
                    {
                        var fileData = Utils.DecompressData(Utils.GetByteArrayForZipArchiveEntry(file));

                        // Todo: Sort upgrading compressed files
                        var fileName = Utils.GetUncompressedFileName(file.FullName);
                        githubFiles.Add(fileName, fileData);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning($"Failed to decompress repository file {file.FullName} in {repository.Name}", ex);
                    }
                } else
                {
                    try
                    {
                        //var document = new XmlDocument();
                        //document.Load(file.Open());

                        //Todo: Sort data upgrading
                        //var blah = Utils.UpgradeDataVersion(document, file.FullName);
                    
                        githubFiles.Add(file.FullName, Utils.GetByteArrayForZipArchiveEntry(file));

                    } catch(Exception ex)
                    {
                        _logger.LogWarning($"Failed to transform repository file {file.FullName} in {repository.Name}", ex);
                    }
                }

            }

            return githubFiles;
        }

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

        private async Task<Repository> GetRepository(string repositoryName)
        {
            var repositories = await GetRepositories();
            
            return repositories.FirstOrDefault(x => x.Name == repositoryName);            
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

        private async Task<IReadOnlyList<Repository>> GetGitHubRepositories()
        {
            _logger.LogInformation("Getting repositories from GitHub");

            var organizationName = _appSettings.GitHub.Organisation;
            var client = GetGitHubClient();

            var repos = await client.Repository.GetAllForOrg(organizationName);
            return repos;
        }

        private async Task<IReadOnlyList<Release>> GetGitHubRelease(Repository repository)
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

        private RepositoryVm CreateRepositoryVm(string baseUrl, Repository repository, Release latestRelease)
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

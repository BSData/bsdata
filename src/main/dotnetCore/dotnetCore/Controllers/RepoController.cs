using System;
using System.Threading.Tasks;
using dotnetCore.Constants;
using dotnetCore.Services;
using dotnetCore.ViewModel;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using ILogger = Microsoft.Extensions.Logging.ILogger;

namespace dotnetCore.Controllers
{
    [Route("repos")]
    [ApiController]
    public class RepoController : ControllerBase
    {
        private readonly ILogger logger;
        private readonly IGitHubService gitHubService;

        public RepoController(ILogger<RepoController> logger, IGitHubService gitHubService)
        {
            this.logger = logger;
            this.gitHubService = gitHubService;
        }

        /// <summary>
        /// Returns the URL of this service request without the path info/conext.
        /// So... http://something.com/REPO_SERVICE_PATH/some/thing becomes http://something.com/REPO_SERVICE_PATH
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        private static string GetBaseUrl(HttpRequest request)
        {
            var url = request.GetEncodedUrl();

            url = url.Substring(0, url.IndexOf(WebConstants.REPO_SERVICE_PATH) + WebConstants.REPO_SERVICE_PATH.Length);
            return url;
        }

        [HttpGet]
        [ProducesResponseType(StatusCodes.Status500InternalServerError)]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesDefaultResponseType]
        public async Task<ActionResult<RepositorySourceVm>> GetRepositories()
        {
            try
            {
                var url = GetBaseUrl(Request);

                var repositoryList = await gitHubService.GetRepos(url);

                return Ok(repositoryList);
            }
            catch (Exception ex)
            {
                logger.LogError($"Failed to load repo list", ex);
                return StatusCode(500);
            }
        }

        [HttpGet("{repoName}")]
        public async Task<ActionResult> GetRepositoryDetails(string repoName)
        {
            var repositoryVm = new RepositoryVm();

            if (string.IsNullOrWhiteSpace(repoName))
            {
                repositoryVm = new RepositoryVm();
                repositoryVm.ErrorMessage = "You must provide a repository name.";
                return Ok(repositoryVm);
            }

            try
            {
                repositoryVm = await gitHubService.GetRepoFiles(GetBaseUrl(Request), repoName);
            }
            catch (Exception ex)
            {
                // Todo: this
            }

            if (repositoryVm == null)
            {
                throw new NullReferenceException($"Could not find repository named {repoName}");
            }

            return Ok(repositoryVm);
        }

    }
}

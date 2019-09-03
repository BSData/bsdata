using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using dotnetCore.Constants;
using dotnetCore.Services;
using dotnetCore.ViewModel;
using Google.Apis.Logging;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using ILogger = Microsoft.Extensions.Logging.ILogger;

namespace dotnetCore.Controllers
{
    [Route("api/repos")]
    [ApiController]
    public class RepoController : ControllerBase
    {
        private readonly ILogger _logger;
        private readonly IGitHubService _gitHubService;

        public RepoController(ILogger<RepoController> logger, IGitHubService gitHubService)
        {
            _logger = logger;
            _gitHubService = gitHubService;
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
        public async Task<ActionResult<RepositorySourceVm>> GetRepos()
        {
            _logger.LogInformation("Message displayed: Test message");

            var url = GetBaseUrl(Request);

            var repositoryList = await _gitHubService.GetRepos(url);

            return Ok(repositoryList);
        }
    }
}
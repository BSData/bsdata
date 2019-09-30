using dotnetCore.ViewModel;
using Octokit;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace dotnetCore.Services
{
    public interface IGitHubService
    {
        Task<RepositorySourceVm> GetRepos(string baseUrl);
        Task<IReadOnlyList<Repository>> RefreshRepositoriesAsync();
        Task<RepositoryVm> GetRepoFiles(string baseUrl, string repositoryName);
    }
}
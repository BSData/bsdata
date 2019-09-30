using System.Collections.Generic;
using System.Threading.Tasks;
using dotnetCore.Models;

namespace dotnetCore.Services
{
    public interface IIndexerService
    {
        Dictionary<string, DataFile> CreateRepositoryData(string repositoryName, string baseUrl, List<string> repositoryUrls, Dictionary<string, byte[]> fileDatas);
    }
}
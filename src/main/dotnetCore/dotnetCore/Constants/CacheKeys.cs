using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Constants
{
    public static class CacheKeys
    {
        public const string Repositories = "Repositories";
        public const string RepositoryReleases = "RepositoryReleases";
        public const string NextReposUpdateDate = "NextReposUpdateDate";

        public const string RepositoryFiles = "RepositoryFiles";
        public const string RepositoryFeedEntries = "RepositoryFeedEntries";
        public const string RepositoryReleaseDates = "RepositoryReleaseDates";
        public const string DataRequiresUpdateFlags = "DataRequiresUpdateFlags";
    }
}

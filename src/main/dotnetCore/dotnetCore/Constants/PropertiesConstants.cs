using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Constants
{
    /**
     * Constants for .properties files
     */
    public static class PropertiesConstants
    {
        public const string LOGGER_NAME = "org.battlescribedata";
    
        public const string APPLICATION_PROPERTIES_FILE_PATH = "application.properties";
        public const string GITHUB_USER_PROPERTIES_FILE_PATH = "github-user.properties";
    
        public const string GITHUB_ANON_USERNAME = "github.anon.username";
        public const string GITHUB_ANON_EMAIL = "github.anon.email";
        public const string GITHUB_ANON_TOKEN = "github.anon.token";
    
        public const string GITHUB_ORGANIZATION = "github.organisation";
        public const string GITHUB_MASTER_BRANCH = "github.master.branch";
        public const string GITHUB_BSDATA_REPO = "github.bsdata.repo";
    
        public const string SITE_NAME = "site.name";
        public const string SITE_DESCRIPTION = "site.description";
        public const string SITE_WEBSITE_URL = "site.website.url";
        public const string SITE_GITHUB_URL = "site.github.url";
        public const string SITE_DISCORD_URL = "site.discord.url";
        public const string SITE_TWITTER_URL = "site.twitter.url";
        public const string SITE_FACEBOOK_URL = "site.facebook.url";
    }
}

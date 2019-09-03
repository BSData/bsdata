using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace dotnetCore.Utilities
{
    public static class Utils
    {

        public static string CheckUrl(String url)
        {
            if (string.IsNullOrWhiteSpace(url))
            {
                return null;
            }

            url = url.Trim().Replace(" ", "%20");
            if (!url.Contains("://"))
            {
                url = "http://" + url;
            }

            if (url == "http://")
            {
                return null;
            }

            try
            {
                Uri uri = new Uri(url);
            }
            catch (Exception ex)
            {
                return null;
            }

            return url;
        }
    }
}

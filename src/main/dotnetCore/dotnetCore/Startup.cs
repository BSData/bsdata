using System;
using dotnetCore.Services;
using Google.Cloud.Diagnostics.AspNetCore;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace dotnetCore
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
            GoogleCloud = GetGoogleCloudPlatformInfo();
        }

        public IConfiguration Configuration { get; }

        private (string ProjectId, string ServiceName, string Version) GoogleCloud { get; }

        private bool HasGcpProjectId => !string.IsNullOrEmpty(GoogleCloud.ProjectId);

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMemoryCache();
            services.AddControllers();
            services.AddScoped<IGitHubService, GitHubService>();
            services.AddScoped<IIndexerService, IndexerService>();

            if (HasGcpProjectId)
            {
                // Enables Stackdriver Trace.
                services.AddGoogleTrace(options => options.ProjectId = GoogleCloud.ProjectId);
                // Sends Exceptions to Stackdriver Error Reporting.
                services.AddGoogleExceptionLogging(
                    options =>
                    {
                        options.ProjectId = GoogleCloud.ProjectId;
                        options.ServiceName = GoogleCloud.ServiceName;
                        options.Version = GoogleCloud.Version;
                    });
                services.AddSingleton<ILoggerProvider>(
                    _ => GoogleLoggerProvider.Create(GoogleCloud.ProjectId));
            }

            var appSettings = new AppSettings();
            Configuration.Bind("AppSettings", appSettings);
            services.AddSingleton(appSettings);
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            var logger = app.ApplicationServices.GetService<ILoggerFactory>().CreateLogger("Startup");

            if (HasGcpProjectId)
            {
                logger.LogInformation("Stackdriver Logging enabled: https://console.cloud.google.com/logs/");

                // Sends logs to Stackdriver Error Reporting.
                app.UseGoogleExceptionLogging();
                logger.LogInformation(
                    "Stackdriver Error Reporting enabled: https://console.cloud.google.com/errors/");
                // Sends logs to Stackdriver Trace.
                app.UseGoogleTrace();
                logger.LogInformation("Stackdriver Trace enabled: https://console.cloud.google.com/traces/");
            }
            else
            {
                logger.LogWarning(
                    "Stackdriver Logging not enabled. Missing Google:ProjectId in configuration.");
                logger.LogWarning(
                    "Stackdriver Error Reporting not enabled. Missing Google:ProjectId in configuration.");
                logger.LogWarning(
                    "Stackdriver Trace not enabled. Missing Google:ProjectId in configuration.");
            }

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }

        private (string ProjectId, string ServiceName, string Version) GetGoogleCloudPlatformInfo()
        {
            var projectId = GetProjectId(Configuration);
            if (string.IsNullOrWhiteSpace(projectId))
            {
                return (projectId, null, null);
            }
            var serviceName = GetServiceName(Configuration);
            var version = GetVersion(Configuration);
            return (projectId, serviceName, version);
        }

        /// <summary>
        /// Get the Google Cloud Platform Project ID from the platform it is running on,
        /// or from the appsettings.json configuration if not running on Google Cloud Platform.
        /// </summary>
        /// <param name="config">The appsettings.json configuration.</param>
        /// <returns>
        /// The ID of the GCP Project this service is running on, or the Google:ProjectId
        /// from the configuration if not running on GCP.
        /// </returns>
        private static string GetProjectId(IConfiguration config)
        {
            var instance = Google.Api.Gax.Platform.Instance();
            var projectId = instance?.ProjectId ?? config["Google:ProjectId"];
            if (string.IsNullOrEmpty(projectId))
            {
                // Set Google:ProjectId in appsettings.json to enable stackdriver logging outside of GCP.
                return null;
            }
            return projectId;
        }

        /// <summary>
        /// Gets a service name for error reporting.
        /// </summary>
        /// <param name="config">The appsettings.json configuration to read a service name from.</param>
        /// <returns>
        /// The name of the Google App Engine service hosting this application,
        /// or the Google:ErrorReporting:ServiceName configuration field if running elsewhere.
        /// </returns>
        /// <seealso href="https://cloud.google.com/error-reporting/docs/formatting-error-messages#FIELDS.service"/>
        private static string GetServiceName(IConfiguration config)
        {
            var instance = Google.Api.Gax.Platform.Instance();
            var serviceName = instance?.GaeDetails?.ServiceId ?? config["Google:ErrorReporting:ServiceName"];
            if (string.IsNullOrEmpty(serviceName))
            {
                throw new InvalidOperationException(
                    "The error reporting library requires a service name. " +
                    "Update appsettings.json by setting the Google:ErrorReporting:ServiceName property with your " +
                    "Service Id, then recompile.");
            }
            return serviceName;
        }

        /// <summary>
        /// Gets a version id for error reporting.
        /// </summary>
        /// <param name="config">The appsettings.json configuration to read a version id from.</param>
        /// <returns>
        /// The version of the Google App Engine service hosting this application,
        /// or the Google:ErrorReporting:Version configuration field if running elsewhere.
        /// </returns>
        /// <seealso href="https://cloud.google.com/error-reporting/docs/formatting-error-messages#FIELDS.version"/>
        private static string GetVersion(IConfiguration config)
        {
            var instance = Google.Api.Gax.Platform.Instance();
            var versionId = instance?.GaeDetails?.VersionId ?? config["Google:ErrorReporting:Version"];
            if (string.IsNullOrEmpty(versionId))
            {
                throw new InvalidOperationException(
                    "The error reporting library requires a version id. " +
                    "Update appsettings.json by setting the Google:ErrorReporting:Version property with your " +
                    "service version id, then recompile.");
            }
            return versionId;
        }
    }
}

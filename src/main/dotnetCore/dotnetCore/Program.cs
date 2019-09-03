using Google.Cloud.Diagnostics.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using System;
using System.IO;
using dotnetCore.Services;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.AspNetCore.Mvc.Infrastructure;

namespace dotnetCore
{
    public class Program
    {
        public static IHostingEnvironment HostingEnvironment { get; private set; }
        public static IConfiguration Configuration { get; private set; }

        public static string GcpProjectId { get; private set; }
        public static bool HasGcpProjectId => !string.IsNullOrEmpty(GcpProjectId);

        public static async Task Main(string[] args)
        {
            var host = new WebHostBuilder()
                .UseKestrel()
                .UseContentRoot(Directory.GetCurrentDirectory())
                .UseIISIntegration()
                .ConfigureAppConfiguration((context, configBuilder) =>
                {
                    HostingEnvironment = context.HostingEnvironment;

                    configBuilder.SetBasePath(HostingEnvironment.ContentRootPath)
                        .AddJsonFile("appsettings.json", optional: true, reloadOnChange: true)
                        .AddJsonFile($"appsettings.{HostingEnvironment.EnvironmentName}.json", optional: true)
                        .AddJsonFile($"github-user.json", optional: true)
                        .AddEnvironmentVariables();

                    Configuration = configBuilder.Build();
                    GcpProjectId = GetProjectId(Configuration);
                })
                .ConfigureServices(services =>
                {
                    //services.AddConfiguration();
                    services.AddMemoryCache();
                    // Add framework services.Microsoft.VisualStudio.ExtensionManager.ExtensionManagerService
                    services.AddMvc();
                    services.AddScoped<IGitHubService, GitHubService>();

                    if (HasGcpProjectId)
                    {
                        // Enables Stackdriver Trace.
                        services.AddGoogleTrace(options => options.ProjectId = GcpProjectId);
                        // Sends Exceptions to Stackdriver Error Reporting.
                        services.AddGoogleExceptionLogging(
                            options =>
                            {
                                options.ProjectId = GcpProjectId;
                                options.ServiceName = GetServiceName(Configuration);
                                options.Version = GetVersion(Configuration);
                            });
                        services.AddSingleton<ILoggerProvider>(sp => GoogleLoggerProvider.Create(GcpProjectId));
                    }

                    var appSettings = new AppSettings();
                    Configuration.Bind("AppSettings", appSettings);
                    services.AddSingleton(appSettings);


                })
                .ConfigureLogging(loggingBuilder =>
                {
                    loggingBuilder.AddConfiguration(Configuration.GetSection("Logging"));
                    if (HostingEnvironment.IsDevelopment())
                    {
                        // Only use Console and Debug logging during development.
                        loggingBuilder.AddConsole(options =>
                            options.IncludeScopes = Configuration.GetValue<bool>("Logging:IncludeScopes"));
                        loggingBuilder.AddDebug();
                        loggingBuilder.AddEventSourceLogger();
                    }
                })
                .Configure((app) =>
                {
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

                    app.UseMvc();
                })
                .Build();

            // Initialise the Repo Cache on app start up.
            using (var scope = host.Services.CreateScope())
            {
                // Get the DbContext instance
                var gitHubService = scope.ServiceProvider.GetRequiredService<IGitHubService>();

                await gitHubService.RefreshRepositoriesAsync();
            }

            host.Run();
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

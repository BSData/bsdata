using System.Threading.Tasks;
using dotnetCore.Services;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace dotnetCore
{
    public static class Program
    {
        public static async Task Main(string[] args)
        {
            var host = CreateHostBuilder(args).Build();
            await PrimeCache(host);
            host.Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureLogging((ctx, logginBuilder) =>
                {
                    logginBuilder.AddConfiguration(ctx.Configuration.GetSection("Logging"));
                    if (ctx.HostingEnvironment.IsDevelopment())
                    {
                        // Only use Console and Debug logging during development.
                        logginBuilder.AddConsole(options =>
                            options.IncludeScopes = ctx.Configuration.GetValue<bool>("Logging:IncludeScopes"));
                        logginBuilder.AddDebug();
                        logginBuilder.AddEventSourceLogger();
                    }
                })
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    webBuilder.UseStartup<Startup>();
                });

        private static async Task PrimeCache(IHost host)
        {
            // Initialise the Repo Cache on app start up.
            using var scope = host.Services.CreateScope();
            var gitHubService = scope.ServiceProvider.GetRequiredService<IGitHubService>();
            await gitHubService.RefreshRepositoriesAsync();
        }
    }
}

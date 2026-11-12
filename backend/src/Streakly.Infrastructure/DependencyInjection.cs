namespace Streakly.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Streakly.Application.Interfaces;
using Streakly.Infrastructure.Persistence;
using Streakly.Infrastructure.Persistence.Repositories;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddDbContext<AppDbContext>(options=> options.UseNpgsql(configuration.GetConnectionString("DefaultConnection")));
        services.AddScoped<IGoalRepository, GoalRepository>();
        return services;
    }
}
namespace Streakly.Application;
using Microsoft.Extensions.DependencyInjection;
using Streakly.Application.Interfaces;
using Streakly.Application.Services;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IGoalService, GoalService>();
        return services;
    }
}

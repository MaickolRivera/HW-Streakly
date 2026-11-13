namespace Streakly.Api.Middleware;
using Microsoft.IdentityModel.JsonWebTokens;
using Streakly.Application.Interfaces;

/// <summary>
/// Registra la última actividad del usuario autenticado. Escribe como mucho
/// una vez cada <see cref="Interval"/> para no tocar la base en cada request.
/// </summary>
public class UserActivityMiddleware
{
    private static readonly TimeSpan Interval = TimeSpan.FromMinutes(5);
    private readonly RequestDelegate _next;

    public UserActivityMiddleware(RequestDelegate next)
    {
        _next = next;
    }

    public async Task InvokeAsync(HttpContext context, IUserRepository users)
    {
        if (context.User.Identity?.IsAuthenticated == true &&
            Guid.TryParse(context.User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value, out var userId))
        {
            await users.TouchActivityAsync(userId, DateTime.UtcNow - Interval);
        }

        await _next(context);
    }
}

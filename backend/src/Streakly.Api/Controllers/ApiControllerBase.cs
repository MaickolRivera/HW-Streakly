namespace Streakly.Api.Controllers;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.JsonWebTokens;
using Streakly.Application.Exceptions;

[ApiController]
[Authorize]
public abstract class ApiControllerBase : ControllerBase
{
    protected Guid UserId
    {
        get
        {
            var sub = User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;
            return Guid.TryParse(sub, out var id)
                ? id
                : throw new UnauthorizedException("Token inválido.");
        }
    }

    /// <summary>
    /// La app envia su fecha local para que "hoy" coincida con la del usuario.
    /// </summary>
    protected static DateOnly TodayOr(DateOnly? date) => date ?? DateOnly.FromDateTime(DateTime.UtcNow);
}

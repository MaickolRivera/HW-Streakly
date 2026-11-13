namespace Streakly.Api.Controllers;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;
using Streakly.Domain.Enums;

/// <summary>Endpoints exclusivos para administradores.</summary>
[Route("api/admin")]
[Authorize(Roles = nameof(UserRole.Admin))]
[Tags("Admin")]
public class AdminController : ApiControllerBase
{
    private readonly IAdminService _adminService;

    public AdminController(IAdminService adminService)
    {
        _adminService = adminService;
    }

    /// <summary>Lista los usuarios activos.</summary>
    /// <remarks>
    /// Un usuario está activo si usó la API (inició sesión o hizo cualquier request autenticado)
    /// dentro de los últimos <c>days</c> días. Ordenados del más reciente al más antiguo.
    /// </remarks>
    /// <param name="days">Periodo en días hacia atrás (1 a 365). Por defecto 7.</param>
    [HttpGet("users/active")]
    [ProducesResponseType<List<ActiveUserResponse>>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<List<ActiveUserResponse>>> GetActiveUsers([FromQuery] int days = 7)
    {
        return Ok(await _adminService.GetActiveUsersAsync(days));
    }
}

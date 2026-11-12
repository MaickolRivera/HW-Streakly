namespace Streakly.Api.Controllers;
using Microsoft.AspNetCore.Mvc;
using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;

[Route("api")]
public class StatsController : ApiControllerBase
{
    private readonly IHabitService _habitService;

    public StatsController(IHabitService habitService)
    {
        _habitService = habitService;
    }

    /// <summary>Habitos programados para hoy, con progreso y racha global.</summary>
    [HttpGet("today")]
    public async Task<ActionResult<TodayResponse>> Today([FromQuery] DateOnly? date)
    {
        return Ok(await _habitService.GetTodayAsync(UserId, TodayOr(date)));
    }

    /// <summary>Porcentaje de cumplimiento por habito en los ultimos N dias.</summary>
    [HttpGet("stats/compliance")]
    public async Task<ActionResult<List<HabitComplianceResponse>>> Compliance([FromQuery] DateOnly? date, [FromQuery] int days = 30)
    {
        return Ok(await _habitService.GetComplianceAsync(UserId, TodayOr(date), days));
    }

    [HttpGet("stats/summary")]
    public async Task<ActionResult<SummaryResponse>> Summary([FromQuery] DateOnly? date)
    {
        return Ok(await _habitService.GetSummaryAsync(UserId, TodayOr(date)));
    }
}

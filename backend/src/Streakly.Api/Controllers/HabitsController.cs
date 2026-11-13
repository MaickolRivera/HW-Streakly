namespace Streakly.Api.Controllers;
using Microsoft.AspNetCore.Mvc;
using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;
using Streakly.Domain.Enums;

[Route("api/habits")]
public class HabitsController : ApiControllerBase
{
    private readonly IHabitService _habitService;

    public HabitsController(IHabitService habitService)
    {
        _habitService = habitService;
    }

    /// <summary>Lista los habitos del usuario, con busqueda y filtro opcionales.</summary>
    /// <param name="search">Texto contenido en el nombre (no distingue mayusculas).</param>
    /// <param name="type">Boolean (hecho / no hecho) o Counter (contador).</param>
    [HttpGet]
    public async Task<ActionResult<List<HabitResponse>>> GetAll([FromQuery] string? search, [FromQuery] HabitType? type)
    {
        return Ok(await _habitService.GetAllAsync(UserId, search, type));
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<HabitResponse>> GetById(Guid id)
    {
        return Ok(await _habitService.GetByIdAsync(UserId, id));
    }

    [HttpPost]
    public async Task<ActionResult<HabitResponse>> Create(CreateHabitRequest request)
    {
        var habit = await _habitService.CreateAsync(UserId, request);
        return CreatedAtAction(nameof(GetById), new { id = habit.Id }, habit);
    }

    /// <summary>Edita nombre, icono, minimo diario, unidad, dias y recordatorio. El tipo no cambia.</summary>
    [HttpPut("{id:guid}")]
    public async Task<ActionResult<HabitResponse>> Update(Guid id, UpdateHabitRequest request)
    {
        return Ok(await _habitService.UpdateAsync(UserId, id, request));
    }

    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        await _habitService.DeleteAsync(UserId, id);
        return NoContent();
    }

    /// <summary>Crea o reemplaza el registro del dia indicado.</summary>
    [HttpPut("{id:guid}/entries")]
    public async Task<ActionResult<HabitEntryResponse>> SetEntry(Guid id, RegisterEntryRequest request)
    {
        return Ok(await _habitService.SetEntryAsync(UserId, id, request));
    }

    [HttpDelete("{id:guid}/entries/{date}")]
    public async Task<IActionResult> DeleteEntry(Guid id, DateOnly date)
    {
        await _habitService.DeleteEntryAsync(UserId, id, date);
        return NoContent();
    }

    [HttpGet("{id:guid}/stats")]
    public async Task<ActionResult<HabitStatsResponse>> Stats(Guid id, [FromQuery] DateOnly? date, [FromQuery] int weeks = 18)
    {
        return Ok(await _habitService.GetStatsAsync(UserId, id, TodayOr(date), weeks));
    }
}

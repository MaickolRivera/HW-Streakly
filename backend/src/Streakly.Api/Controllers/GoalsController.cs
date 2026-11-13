namespace Streakly.Api.Controllers;
using Microsoft.AspNetCore.Mvc;
using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;

[Route("api/goals")]
public class GoalsController : ApiControllerBase
{
    private readonly IGoalService _goalService;

    public GoalsController(IGoalService goalService)
    {
        _goalService = goalService;
    }

    /// <summary>Lista los goals del usuario, con busqueda y filtro opcionales.</summary>
    /// <param name="search">Texto contenido en el titulo o la descripcion.</param>
    /// <param name="status">All (todos), Active (en curso) o Completed (todas sus metas cumplidas).</param>
    [HttpGet]
    public async Task<ActionResult<List<GoalResponse>>> GetAll([FromQuery] string? search, [FromQuery] GoalStatusFilter status = GoalStatusFilter.All)
    {
        return Ok(await _goalService.GetAllAsync(UserId, search, status));
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<GoalResponse>> GetById(Guid id)
    {
        return Ok(await _goalService.GetByIdAsync(UserId, id));
    }

    [HttpPost]
    public async Task<ActionResult<GoalResponse>> Create(CreateGoalRequest request)
    {
        var goal = await _goalService.CreateAsync(UserId, request);
        return CreatedAtAction(nameof(GetById), new { id = goal.Id }, goal);
    }

    /// <summary>Edita titulo, descripcion y fecha limite.</summary>
    [HttpPut("{id:guid}")]
    public async Task<ActionResult<GoalResponse>> Update(Guid id, UpdateGoalRequest request)
    {
        return Ok(await _goalService.UpdateAsync(UserId, id, request));
    }

    /// <summary>Cambia el titulo de una meta.</summary>
    [HttpPut("{id:guid}/tasks/{taskId:guid}")]
    public async Task<ActionResult<GoalResponse>> RenameTask(Guid id, Guid taskId, UpdateTaskRequest request)
    {
        return Ok(await _goalService.RenameTaskAsync(UserId, id, taskId, request));
    }

    [HttpPost("{id:guid}/tasks")]
    public async Task<ActionResult<GoalResponse>> AddTask(Guid id, AddTaskRequest request)
    {
        return Ok(await _goalService.AddTaskAsync(UserId, id, request));
    }

    [HttpPatch("{id:guid}/tasks/{taskId:guid}/toggle")]
    public async Task<ActionResult<GoalResponse>> ToggleTask(Guid id, Guid taskId)
    {
        return Ok(await _goalService.ToggleTaskAsync(UserId, id, taskId));
    }

    [HttpDelete("{id:guid}/tasks/{taskId:guid}")]
    public async Task<ActionResult<GoalResponse>> RemoveTask(Guid id, Guid taskId)
    {
        return Ok(await _goalService.RemoveTaskAsync(UserId, id, taskId));
    }

    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        await _goalService.DeleteAsync(UserId, id);
        return NoContent();
    }
}

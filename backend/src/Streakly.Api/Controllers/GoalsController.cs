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

    [HttpGet]
    public async Task<ActionResult<List<GoalResponse>>> GetAll()
    {
        return Ok(await _goalService.GetAllAsync(UserId));
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

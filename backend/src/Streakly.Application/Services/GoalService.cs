namespace Streakly.Application.Services;

using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
using Streakly.Application.Mappings;
using Streakly.Application.Exceptions;

public class GoalService : IGoalService
{
    private readonly IGoalRepository _goalRepository;

    public GoalService(IGoalRepository goalRepository)
    {
        _goalRepository = goalRepository;
    }
    private async Task<Goal> GetOwnedGoalAsync(Guid userId, Guid goalId)
    {
        var goal = await _goalRepository.GetByIdAsync(goalId);
        if (goal == null || goal.UserId != userId)
        {
            throw new NotFoundException($"No se encontró la meta con ID {goalId}");
        }
        return goal;
    }
    public async Task<GoalResponse> CreateAsync(Guid userId, CreateGoalRequest request)
    {
        var goal = new Goal(userId, request.Title, request.Description, request.DueDate);
        foreach (var task in request.Tasks ?? [])
        {
            if (!string.IsNullOrWhiteSpace(task))
                goal.AddTask(task);
        }
        await _goalRepository.AddAsync(goal);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> GetByIdAsync(Guid userId, Guid goalId)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> AddTaskAsync(Guid userId, Guid goalId, AddTaskRequest request)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.AddTask(request.Title);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> CompleteTaskAsync(Guid userId, Guid goalId, Guid taskId)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.CompleteTask(taskId);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> ToggleTaskAsync(Guid userId, Guid goalId, Guid taskId)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.ToggleTask(taskId);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> RemoveTaskAsync(Guid userId, Guid goalId, Guid taskId)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.RemoveTask(taskId);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task DeleteAsync(Guid userId, Guid goalId)
    {
        await GetOwnedGoalAsync(userId, goalId);
        await _goalRepository.DeleteAsync(goalId);
    }

    public async Task<GoalResponse> UpdateAsync(Guid userId, Guid goalId, UpdateGoalRequest request)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.Update(request.Title, request.Description, request.DueDate);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task<GoalResponse> RenameTaskAsync(Guid userId, Guid goalId, Guid taskId, UpdateTaskRequest request)
    {
        var goal = await GetOwnedGoalAsync(userId, goalId);
        goal.RenameTask(taskId, request.Title);
        await _goalRepository.UpdateAsync(goal);
        return goal.ToResponse();
    }

    public async Task<List<GoalResponse>> GetAllAsync(Guid userId, string? search = null, GoalStatusFilter status = GoalStatusFilter.All)
    {
        var goals = await _goalRepository.GetByUserIdAsync(userId);
        var term = search?.Trim();

        return goals
            .Where(g => string.IsNullOrEmpty(term)
                || g.Title.Contains(term, StringComparison.OrdinalIgnoreCase)
                || (g.Description?.Contains(term, StringComparison.OrdinalIgnoreCase) ?? false))
            .Where(g => status switch
            {
                GoalStatusFilter.Active => !g.IsCompleted,
                GoalStatusFilter.Completed => g.IsCompleted,
                _ => true
            })
            .OrderBy(g => g.DueDate ?? DateOnly.MaxValue)
            .ThenBy(g => g.CreatedAt)
            .Select(g => g.ToResponse())
            .ToList();
    }
}

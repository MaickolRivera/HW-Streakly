namespace Streakly.Application.Interfaces;
using Streakly.Application.DTOs;

public interface IGoalService
{
    Task<GoalResponse> CreateAsync(Guid userId, CreateGoalRequest request);
    Task<GoalResponse> GetByIdAsync(Guid userId, Guid goalId);
    Task<List<GoalResponse>> GetAllAsync(Guid userId);
    Task<GoalResponse> AddTaskAsync(Guid userId, Guid goalId, AddTaskRequest request);
    Task<GoalResponse> CompleteTaskAsync(Guid userId, Guid goalId, Guid taskId);
    Task<GoalResponse> ToggleTaskAsync(Guid userId, Guid goalId, Guid taskId);
    Task<GoalResponse> RemoveTaskAsync(Guid userId, Guid goalId, Guid taskId);
    Task DeleteAsync(Guid userId, Guid goalId);
}

namespace Streakly.Application.DTOs;

public record CreateGoalRequest(string Title, string? Description, DateOnly? DueDate, List<string>? Tasks);
public record UpdateGoalRequest(string Title, string? Description, DateOnly? DueDate);
public record AddTaskRequest(string Title);
public record UpdateTaskRequest(string Title);

/// <summary>Filtro de goals por estado.</summary>
public enum GoalStatusFilter { All, Active, Completed }
public record GoalResponse(
    Guid Id,
    string Title,
    string? Description,
    DateOnly? DueDate,
    DateTime CreatedAt,
    int CompletedTasks,
    int TotalTasks,
    int Progress,
    List<GoalTaskResponse> Tasks);
public record GoalTaskResponse(Guid Id, string Title, bool IsCompleted);

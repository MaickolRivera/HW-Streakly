namespace Streakly.Application.DTOs;

public record CreateGoalRequest(string Title);
public record AddTaskRequest(string Title);
public record GoalResponse(Guid Id, string Title, DateTime CreatedAt, List<GoalTaskResponse> Tasks);  
public record GoalTaskResponse(Guid Id, string Title, bool IsCompleted);
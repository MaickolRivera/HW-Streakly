namespace Streakly.Application.Mappings;
using Streakly.Application.DTOs;
using Streakly.Domain.Entities;

public static class GoalMappings
{
    public static GoalResponse ToResponse(this Goal goal)
    {
        var total = goal.Tasks.Count;
        var completed = goal.CompletedTasks;
        return new GoalResponse(
            goal.Id,
            goal.Title,
            goal.Description,
            goal.DueDate,
            goal.CreatedAt,
            completed,
            total,
            total == 0 ? 0 : (int)Math.Round(completed * 100.0 / total),
            goal.Tasks
                .OrderBy(t => t.Position)
                .Select(t => new GoalTaskResponse(t.Id, t.Title, t.IsCompleted))
                .ToList()
        );
    }
}

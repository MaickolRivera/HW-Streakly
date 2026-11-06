namespace Streakly.Application.Mappings;
using Streakly.Application.DTOs;
using Streakly.Domain.Entities;

public static class GoalMappings
{
    public static GoalResponse ToResponse(this Goal goal)
    {
        return new GoalResponse(
            goal.Id,
            goal.Title,
            goal.CreatedAt,
            goal.Tasks.Select(t=> new GoalTaskResponse(t.Id, t.Title, t.IsCompleted)).ToList()
        );
    }
}
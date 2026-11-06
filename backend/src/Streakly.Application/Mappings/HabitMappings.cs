namespace Streakly.Application.Mappings;
using Streakly.Application.DTOs;
using Streakly.Domain.ValueObjects;
public static class HabitMappings
{
    public static HabitResponse ToResponse(this Domain.Entities.Habit habit)
    {
        return new HabitResponse(
            habit.Id,
            habit.Name,
            habit.Category,
            habit.Type,
            habit.Thresholds?.ToDto(),
            habit.CreatedAt
        );
    }
    public static HabitEntryResponse ToResponse(this Domain.Entities.HabitEntry entry)
    {
        return new HabitEntryResponse(
            entry.Id,
            entry.EntryDate,
            entry.Value
        );
    }
    public static ScaleThresholdsDto ToDto(this ScaleThresholds thresholds)
    {
        return new ScaleThresholdsDto(
            thresholds.Minimum,
            thresholds.Good,
            thresholds.Excellent,
            thresholds.Maximum,
            thresholds.Unit
        );
    }
}
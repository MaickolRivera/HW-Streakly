namespace Streakly.Application.Mappings;
using Streakly.Application.DTOs;
using Streakly.Domain.Enums;
using Streakly.Domain.ValueObjects;
public static class HabitMappings
{
    public static HabitResponse ToResponse(this Domain.Entities.Habit habit)
    {
        return new HabitResponse(
            habit.Id,
            habit.Name,
            habit.Icon,
            habit.Category,
            habit.Type,
            habit.TargetValue,
            habit.Unit,
            habit.ScheduledDays.ToIsoDays(),
            habit.ReminderTime,
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
    public static ScaleThresholds ToValueObject(this ScaleThresholdsDto dto)
    {
        return new ScaleThresholds(dto.Minimum, dto.Good, dto.Excellent, dto.Maximum, dto.Unit);
    }
}

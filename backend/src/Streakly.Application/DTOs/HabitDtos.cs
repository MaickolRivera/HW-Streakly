namespace Streakly.Application.DTOs;
using Streakly.Domain.Enums;
public record CreateHabitRequest(string Name, CategoryType Category, HabitType Type, ScaleThresholdsDto? Thresholds);
public record RegisterEntryRequest(DateOnly Date, decimal Value);
//
public record HabitResponse(Guid Id, string Name, CategoryType Category, HabitType Type, ScaleThresholdsDto? Thresholds, DateTime CreatedAt);
public record HabitEntryResponse(Guid Id, DateOnly Date, decimal Value);
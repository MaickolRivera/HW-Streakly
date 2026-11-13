namespace Streakly.Application.DTOs;
using Streakly.Domain.Enums;

public record CreateHabitRequest(
    string Name,
    string? Icon,
    HabitType Type,
    decimal TargetValue,
    string? Unit,
    List<int>? Days,
    TimeOnly? ReminderTime,
    CategoryType? Category,
    ScaleThresholdsDto? Thresholds);

/// <summary>Datos editables de un habito. El tipo no se puede cambiar.</summary>
public record UpdateHabitRequest(
    string Name,
    string? Icon,
    decimal TargetValue,
    string? Unit,
    List<int>? Days,
    TimeOnly? ReminderTime);

public record RegisterEntryRequest(DateOnly Date, decimal Value);

public record HabitResponse(
    Guid Id,
    string Name,
    string Icon,
    CategoryType Category,
    HabitType Type,
    decimal TargetValue,
    string Unit,
    List<int> Days,
    TimeOnly? ReminderTime,
    ScaleThresholdsDto? Thresholds,
    DateTime CreatedAt);

public record HabitEntryResponse(Guid Id, DateOnly Date, decimal Value);

// Pantalla "Hoy"
public record TodayHabitResponse(
    Guid Id,
    string Name,
    string Icon,
    HabitType Type,
    decimal TargetValue,
    string Unit,
    decimal Value,
    bool Completed,
    int CurrentStreak);

public record TodayResponse(
    DateOnly Date,
    int GlobalStreak,
    int Completed,
    int Total,
    List<TodayHabitResponse> Habits);

// Pantalla "Rachas"
public record HeatmapDayResponse(DateOnly Date, int Level, bool Scheduled);

public record HabitStatsResponse(
    Guid HabitId,
    string Name,
    int CurrentStreak,
    int BestStreak,
    int CompliancePercent,
    int CompletedDays,
    int ScheduledDays,
    int Weeks,
    List<HeatmapDayResponse> Heatmap);

public record HabitComplianceResponse(Guid HabitId, string Name, string Icon, int Percent);

public record SummaryResponse(int GlobalStreak, int BestGlobalStreak, int TotalHabits, int TotalCompletions);

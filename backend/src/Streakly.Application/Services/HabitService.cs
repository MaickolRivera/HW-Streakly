namespace Streakly.Application.Services;

using Streakly.Application.DTOs;
using Streakly.Application.Exceptions;
using Streakly.Application.Interfaces;
using Streakly.Application.Mappings;
using Streakly.Domain.Entities;
using Streakly.Domain.Enums;
using Streakly.Domain.Exceptions;

public class HabitService : IHabitService
{
    private readonly IHabitRepository _habitRepository;

    public HabitService(IHabitRepository habitRepository)
    {
        _habitRepository = habitRepository;
    }

    private async Task<Habit> GetOwnedHabitAsync(Guid userId, Guid habitId)
    {
        var habit = await _habitRepository.GetByIdAsync(habitId);
        if (habit == null || habit.UserId != userId)
        {
            throw new NotFoundException($"No se encontró el hábito con ID {habitId}");
        }
        return habit;
    }

    public async Task<HabitResponse> CreateAsync(Guid userId, CreateHabitRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new DomainException("El nombre del hábito no puede estar vacío.");
        if (await _habitRepository.ExistsByNameAsync(userId, request.Name))
            throw new ConflictException($"Ya tienes un hábito llamado \"{request.Name.Trim()}\".");

        var days = ParseDays(request.Days);

        var habit = new Habit(
            userId,
            request.Name,
            request.Icon ?? "spark",
            request.Category ?? CategoryType.Productivity,
            request.Type,
            request.TargetValue,
            request.Unit ?? string.Empty,
            days,
            request.ReminderTime,
            request.Thresholds?.ToValueObject());

        await _habitRepository.AddAsync(habit);
        return habit.ToResponse();
    }

    public async Task<List<HabitResponse>> GetAllAsync(Guid userId, string? search = null, HabitType? type = null)
    {
        var habits = await _habitRepository.GetByUserIdAsync(userId);
        var term = search?.Trim();

        return habits
            .Where(h => string.IsNullOrEmpty(term) || h.Name.Contains(term, StringComparison.OrdinalIgnoreCase))
            .Where(h => type == null || h.Type == type)
            .Select(h => h.ToResponse())
            .ToList();
    }

    public async Task<HabitResponse> UpdateAsync(Guid userId, Guid habitId, UpdateHabitRequest request)
    {
        var habit = await GetOwnedHabitAsync(userId, habitId);
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new DomainException("El nombre del hábito no puede estar vacío.");
        if (await _habitRepository.ExistsByNameAsync(userId, request.Name, excludeHabitId: habitId))
            throw new ConflictException($"Ya tienes un hábito llamado \"{request.Name.Trim()}\".");

        habit.Update(
            request.Name,
            request.Icon ?? habit.Icon,
            request.TargetValue,
            request.Unit ?? string.Empty,
            request.Days is { Count: > 0 } ? ParseDays(request.Days) : habit.ScheduledDays,
            request.ReminderTime);

        await _habitRepository.UpdateAsync(habit);
        return habit.ToResponse();
    }

    private static WeekDays ParseDays(List<int>? days)
    {
        try
        {
            return days is { Count: > 0 } ? WeekDaysExtensions.FromIsoDays(days) : WeekDays.All;
        }
        catch (ArgumentOutOfRangeException ex)
        {
            throw new DomainException(ex.Message.Split(" (Parameter")[0]);
        }
    }

    public async Task<HabitResponse> GetByIdAsync(Guid userId, Guid habitId)
    {
        var habit = await GetOwnedHabitAsync(userId, habitId);
        return habit.ToResponse();
    }

    public async Task DeleteAsync(Guid userId, Guid habitId)
    {
        await GetOwnedHabitAsync(userId, habitId);
        await _habitRepository.DeleteAsync(habitId);
    }

    public async Task<HabitEntryResponse> SetEntryAsync(Guid userId, Guid habitId, RegisterEntryRequest request)
    {
        var habit = await GetOwnedHabitAsync(userId, habitId);
        var existing = await _habitRepository.GetEntryByDateAsync(habitId, request.Date);

        if (existing == null)
        {
            var entry = habit.RegisterEntry(request.Date, request.Value);
            await _habitRepository.AddEntryAsync(entry);
            return entry.ToResponse();
        }

        habit.UpdateEntry(existing, request.Value);
        await _habitRepository.UpdateEntryAsync(existing);
        return existing.ToResponse();
    }

    public async Task DeleteEntryAsync(Guid userId, Guid habitId, DateOnly date)
    {
        await GetOwnedHabitAsync(userId, habitId);
        var entry = await _habitRepository.GetEntryByDateAsync(habitId, date);
        if (entry == null) return;
        await _habitRepository.DeleteEntryAsync(entry);
    }

    public async Task<TodayResponse> GetTodayAsync(Guid userId, DateOnly today)
    {
        var habits = await _habitRepository.GetByUserIdAsync(userId);
        var entries = await _habitRepository.GetEntriesByUserAsync(userId, DateOnly.MinValue, today);
        var byHabit = entries.ToLookup(e => e.HabitId);

        var items = new List<TodayHabitResponse>();
        foreach (var habit in habits.Where(h => h.IsScheduledOn(today)))
        {
            var habitEntries = byHabit[habit.Id].ToList();
            var completed = CompletedDays(habit, habitEntries);
            var todayValue = habitEntries.FirstOrDefault(e => e.EntryDate == today)?.Value ?? 0;
            var streak = StreakCalculator.CurrentStreak(habit.IsScheduledOn, completed, StartDate(habit, habitEntries), today);

            items.Add(new TodayHabitResponse(
                habit.Id, habit.Name, habit.Icon, habit.Type, habit.TargetValue, habit.Unit,
                todayValue, completed.Contains(today), streak));
        }

        var (globalStreak, _) = GlobalStreaks(habits, entries, today);
        return new TodayResponse(today, globalStreak, items.Count(i => i.Completed), items.Count, items);
    }

    public async Task<HabitStatsResponse> GetStatsAsync(Guid userId, Guid habitId, DateOnly today, int weeks)
    {
        weeks = Math.Clamp(weeks, 1, 53);
        var habit = await GetOwnedHabitAsync(userId, habitId);
        var entries = await _habitRepository.GetEntriesAsync(habitId, DateOnly.MinValue, today);
        var completed = CompletedDays(habit, entries);
        var start = StartDate(habit, entries);

        var current = StreakCalculator.CurrentStreak(habit.IsScheduledOn, completed, start, today);
        var best = StreakCalculator.BestStreak(habit.IsScheduledOn, completed, start, today);

        var last30From = Max(start, today.AddDays(-29));
        var (done30, scheduled30) = StreakCalculator.Compliance(habit.IsScheduledOn, completed, last30From, today);

        // El heatmap arranca en el lunes de hace (weeks - 1) semanas.
        var mondayThisWeek = today.AddDays(-(((int)today.DayOfWeek + 6) % 7));
        var heatmapFrom = mondayThisWeek.AddDays(-7 * (weeks - 1));
        var (doneWindow, scheduledWindow) = StreakCalculator.Compliance(habit.IsScheduledOn, completed, Max(start, heatmapFrom), today);

        var values = entries.ToDictionary(e => e.EntryDate, e => e.Value);
        var heatmap = new List<HeatmapDayResponse>();
        for (var day = heatmapFrom; day <= today; day = day.AddDays(1))
        {
            values.TryGetValue(day, out var value);
            heatmap.Add(new HeatmapDayResponse(day, Level(habit, value), habit.IsScheduledOn(day)));
        }

        return new HabitStatsResponse(
            habit.Id, habit.Name, current, best,
            StreakCalculator.Percent(done30, scheduled30),
            doneWindow, scheduledWindow, weeks, heatmap);
    }

    public async Task<List<HabitComplianceResponse>> GetComplianceAsync(Guid userId, DateOnly today, int days)
    {
        days = Math.Clamp(days, 1, 365);
        var habits = await _habitRepository.GetByUserIdAsync(userId);
        var entries = await _habitRepository.GetEntriesByUserAsync(userId, DateOnly.MinValue, today);
        var byHabit = entries.ToLookup(e => e.HabitId);

        return habits
            .Select(habit =>
            {
                var habitEntries = byHabit[habit.Id].ToList();
                var completed = CompletedDays(habit, habitEntries);
                var from = Max(StartDate(habit, habitEntries), today.AddDays(-(days - 1)));
                var (done, scheduled) = StreakCalculator.Compliance(habit.IsScheduledOn, completed, from, today);
                return new HabitComplianceResponse(habit.Id, habit.Name, habit.Icon, StreakCalculator.Percent(done, scheduled));
            })
            .OrderByDescending(c => c.Percent)
            .ThenBy(c => c.Name)
            .ToList();
    }

    public async Task<SummaryResponse> GetSummaryAsync(Guid userId, DateOnly today)
    {
        var habits = await _habitRepository.GetByUserIdAsync(userId);
        var entries = await _habitRepository.GetEntriesByUserAsync(userId, DateOnly.MinValue, today);
        var habitsById = habits.ToDictionary(h => h.Id);
        var (current, best) = GlobalStreaks(habits, entries, today);
        var totalCompletions = entries.Count(e => habitsById.TryGetValue(e.HabitId, out var h) && h.IsCompletedWith(e.Value));

        return new SummaryResponse(current, best, habits.Count, totalCompletions);
    }

    /// <summary>
    /// Racha global: dias seguidos en los que se cumplio al menos un habito programado.
    /// </summary>
    private static (int Current, int Best) GlobalStreaks(List<Habit> habits, List<HabitEntry> entries, DateOnly today)
    {
        if (habits.Count == 0) return (0, 0);

        var habitsById = habits.ToDictionary(h => h.Id);
        var byHabit = entries.ToLookup(e => e.HabitId);
        var completedDays = entries
            .Where(e => habitsById.TryGetValue(e.HabitId, out var h) && h.IsCompletedWith(e.Value))
            .Select(e => e.EntryDate)
            .ToHashSet();

        var starts = habits.ToDictionary(h => h.Id, h => StartDate(h, byHabit[h.Id].ToList()));
        var start = starts.Values.Min();
        bool IsScheduled(DateOnly day) => habits.Any(h => starts[h.Id] <= day && h.IsScheduledOn(day));

        return (
            StreakCalculator.CurrentStreak(IsScheduled, completedDays, start, today),
            StreakCalculator.BestStreak(IsScheduled, completedDays, start, today));
    }

    private static HashSet<DateOnly> CompletedDays(Habit habit, IEnumerable<HabitEntry> entries) =>
        entries.Where(e => habit.IsCompletedWith(e.Value)).Select(e => e.EntryDate).ToHashSet();

    // El habito "empieza" el dia que se creo, o antes si se registraron dias anteriores.
    private static DateOnly StartDate(Habit habit, List<HabitEntry> entries)
    {
        var created = DateOnly.FromDateTime(habit.CreatedAt);
        if (entries.Count == 0) return created;
        var firstEntry = entries.Min(e => e.EntryDate);
        return firstEntry < created ? firstEntry : created;
    }

    private static DateOnly Max(DateOnly a, DateOnly b) => a > b ? a : b;

    /// <summary>
    /// Intensidad 0-4 para el heatmap: 0 sin registro, 1-2 parcial, 3 cumplido, 4 superado.
    /// </summary>
    private static int Level(Habit habit, decimal value)
    {
        if (value <= 0) return 0;
        return habit.Type switch
        {
            HabitType.Boolean => 4,
            HabitType.Scale => (int)Math.Clamp(value, 1, 4),
            HabitType.Counter when value > habit.TargetValue => 4,
            HabitType.Counter when value >= habit.TargetValue => 3,
            HabitType.Counter when value * 2 >= habit.TargetValue => 2,
            _ => 1
        };
    }
}

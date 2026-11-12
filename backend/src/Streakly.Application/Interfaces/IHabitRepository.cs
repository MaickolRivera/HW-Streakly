namespace Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
public interface IHabitRepository
{
    // Habits
    Task<Habit?> GetByIdAsync(Guid id);
    Task<List<Habit>> GetByUserIdAsync(Guid userId);
    Task<bool> ExistsByNameAsync(Guid userId, string name);
    Task AddAsync(Habit habit);
    Task UpdateAsync(Habit habit);
    Task DeleteAsync(Guid id);

    // Entries
    Task<HabitEntry?> GetEntryByDateAsync(Guid habitId, DateOnly date);
    Task<List<HabitEntry>> GetEntriesAsync(Guid habitId, DateOnly from, DateOnly to);
    Task<List<HabitEntry>> GetEntriesByUserAsync(Guid userId, DateOnly from, DateOnly to);
    Task AddEntryAsync(HabitEntry entry);
    Task UpdateEntryAsync(HabitEntry entry);
    Task DeleteEntryAsync(HabitEntry entry);
}

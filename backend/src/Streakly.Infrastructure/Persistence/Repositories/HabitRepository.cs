namespace Streakly.Infrastructure.Persistence.Repositories;
using Microsoft.EntityFrameworkCore;
using Streakly.Application.Interfaces;
using Streakly.Domain.Entities;

public class HabitRepository : IHabitRepository
{
    private readonly AppDbContext _context;

    public HabitRepository(AppDbContext context)
    {
        _context = context;
    }

    public async Task<Habit?> GetByIdAsync(Guid id)
    {
        return await _context.Habits.FirstOrDefaultAsync(h => h.Id == id);
    }

    public async Task<List<Habit>> GetByUserIdAsync(Guid userId)
    {
        return await _context.Habits
            .Where(h => h.UserId == userId)
            .OrderBy(h => h.CreatedAt)
            .ToListAsync();
    }

    public async Task<bool> ExistsByNameAsync(Guid userId, string name)
    {
        var normalized = name.Trim().ToLower();
        return await _context.Habits.AnyAsync(h => h.UserId == userId && h.Name.ToLower() == normalized);
    }

    public async Task AddAsync(Habit habit)
    {
        _context.Habits.Add(habit);
        await _context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Habit habit)
    {
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Guid id)
    {
        var habit = await _context.Habits.FindAsync(id);
        if (habit == null) return;
        _context.Habits.Remove(habit);
        await _context.SaveChangesAsync();
    }

    public async Task<HabitEntry?> GetEntryByDateAsync(Guid habitId, DateOnly date)
    {
        return await _context.HabitEntries.FirstOrDefaultAsync(e => e.HabitId == habitId && e.EntryDate == date);
    }

    public async Task<List<HabitEntry>> GetEntriesAsync(Guid habitId, DateOnly from, DateOnly to)
    {
        return await _context.HabitEntries
            .Where(e => e.HabitId == habitId && e.EntryDate >= from && e.EntryDate <= to)
            .OrderBy(e => e.EntryDate)
            .ToListAsync();
    }

    public async Task<List<HabitEntry>> GetEntriesByUserAsync(Guid userId, DateOnly from, DateOnly to)
    {
        var habitIds = _context.Habits.Where(h => h.UserId == userId).Select(h => h.Id);
        return await _context.HabitEntries
            .Where(e => habitIds.Contains(e.HabitId) && e.EntryDate >= from && e.EntryDate <= to)
            .OrderBy(e => e.EntryDate)
            .ToListAsync();
    }

    public async Task AddEntryAsync(HabitEntry entry)
    {
        _context.HabitEntries.Add(entry);
        await _context.SaveChangesAsync();
    }

    public async Task UpdateEntryAsync(HabitEntry entry)
    {
        await _context.SaveChangesAsync();
    }

    public async Task DeleteEntryAsync(HabitEntry entry)
    {
        _context.HabitEntries.Remove(entry);
        await _context.SaveChangesAsync();
    }
}

namespace Streakly.Infrastructure.Persistence.Repositories;
using Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
using Microsoft.EntityFrameworkCore;

public class GoalRepository : IGoalRepository
{
    private readonly AppDbContext _context;

    public GoalRepository(AppDbContext context)
    {
        _context = context;
    }
    public async Task<Goal?> GetByIdAsync(Guid id)
    {
        return await _context.Goals.Include(g=>g.Tasks).FirstOrDefaultAsync(g=>g.Id == id);
    }
    public async Task<List<Goal>> GetByUserIdAsync(Guid userId)
    {
        return await _context.Goals.Include(g=>g.Tasks).Where(g=>g.UserId == userId).ToListAsync();
    }
    public async Task AddAsync(Goal goal)
    {
        _context.Goals.Add(goal);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Guid id)
    {
        var goal = await _context.Goals.FindAsync(id);
        if (goal == null) return;
        _context.Goals.Remove(goal);
        await _context.SaveChangesAsync();
    }
    public async Task UpdateAsync(Goal goal)
    {
        await _context.SaveChangesAsync();
    }
}

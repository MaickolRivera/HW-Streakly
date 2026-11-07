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
    public Task<List<Goal>> GetByUserIdAsync(Guid userId)
    {
        throw new NotImplementedException();
    }
    public Task AddAsync(Goal goal)
    {
        throw new NotImplementedException();
    }

    public Task DeleteAsync(Guid id)
    {
        throw new NotImplementedException();
    }
    public Task UpdateAsync(Goal goal)
    {
        throw new NotImplementedException();
    }
}

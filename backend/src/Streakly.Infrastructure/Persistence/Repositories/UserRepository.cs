namespace Streakly.Infrastructure.Persistence.Repositories;
using Microsoft.EntityFrameworkCore;
using Streakly.Application.Interfaces;
using Streakly.Domain.Entities;

public class UserRepository : IUserRepository
{
    private readonly AppDbContext _context;

    public UserRepository(AppDbContext context)
    {
        _context = context;
    }

    public async Task<User?> GetByIdAsync(Guid id)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Id == id);
    }

    public async Task<User?> GetByEmailAsync(string email)
    {
        var normalized = email.Trim().ToLowerInvariant();
        return await _context.Users.FirstOrDefaultAsync(u => u.Email == normalized);
    }

    public async Task<bool> ExistsByEmailAsync(string email)
    {
        var normalized = email.Trim().ToLowerInvariant();
        return await _context.Users.AnyAsync(u => u.Email == normalized);
    }

    public async Task AddAsync(User user)
    {
        _context.Users.Add(user);
        await _context.SaveChangesAsync();
    }

    public async Task UpdateAsync(User user)
    {
        _context.Users.Update(user);
        await _context.SaveChangesAsync();
    }

    public async Task<List<User>> GetActiveSinceAsync(DateTime since)
    {
        return await _context.Users
            .AsNoTracking()
            .Where(u => u.LastActiveAt >= since)
            .OrderByDescending(u => u.LastActiveAt)
            .ToListAsync();
    }

    public async Task TouchActivityAsync(Guid userId, DateTime olderThan)
    {
        var now = DateTime.UtcNow;
        await _context.Users
            .Where(u => u.Id == userId && (u.LastActiveAt == null || u.LastActiveAt < olderThan))
            .ExecuteUpdateAsync(s => s.SetProperty(u => u.LastActiveAt, now));
    }
}

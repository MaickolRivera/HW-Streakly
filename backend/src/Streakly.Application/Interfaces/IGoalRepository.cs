namespace Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
public interface IGoalRepository
{
    Task<Goal?> GetByIdAsync(Guid id);
    Task<List<Goal>> GetByUserIdAsync(Guid userId);
    Task AddAsync(Goal goal);
    Task UpdateAsync(Goal goal);
    Task DeleteAsync(Guid id);
}
namespace Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
public interface IUserRepository
{
    Task<User?> GetByIdAsync(Guid id);
    Task<User?> GetByEmailAsync(string email);
    Task<bool> ExistsByEmailAsync(string email);
    Task AddAsync(User user);
    Task UpdateAsync(User user);
    /// <summary>Usuarios con actividad desde la fecha indicada, del mas reciente al mas antiguo.</summary>
    Task<List<User>> GetActiveSinceAsync(DateTime since);
    /// <summary>Marca actividad solo si la ultima es anterior a <paramref name="olderThan"/> (evita escribir en cada request).</summary>
    Task TouchActivityAsync(Guid userId, DateTime olderThan);
}

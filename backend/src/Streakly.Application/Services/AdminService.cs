namespace Streakly.Application.Services;
using Streakly.Application.DTOs;
using Streakly.Application.Interfaces;
using Streakly.Domain.Exceptions;

public class AdminService : IAdminService
{
    private readonly IUserRepository _userRepository;

    public AdminService(IUserRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<List<ActiveUserResponse>> GetActiveUsersAsync(int days)
    {
        if (days is < 1 or > 365)
            throw new DomainException("El periodo debe estar entre 1 y 365 días.");

        var users = await _userRepository.GetActiveSinceAsync(DateTime.UtcNow.AddDays(-days));
        return users
            .Select(u => new ActiveUserResponse(u.Id, u.Email, u.CreatedAt, u.LastActiveAt!.Value))
            .ToList();
    }
}

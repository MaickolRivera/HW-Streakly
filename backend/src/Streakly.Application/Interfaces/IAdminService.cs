namespace Streakly.Application.Interfaces;
using Streakly.Application.DTOs;

public interface IAdminService
{
    Task<List<ActiveUserResponse>> GetActiveUsersAsync(int days);
}

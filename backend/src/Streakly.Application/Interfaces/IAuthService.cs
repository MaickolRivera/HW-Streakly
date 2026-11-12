namespace Streakly.Application.Interfaces;
using Streakly.Application.DTOs;
using Streakly.Domain.Entities;

public interface IAuthService
{
    Task<AuthResponse> RegisterAsync(RegisterRequest request);
    Task<AuthResponse> LoginAsync(LoginRequest request);
    Task<UserResponse> GetCurrentUserAsync(Guid userId);
}

public interface IPasswordHasher
{
    string Hash(string password);
    bool Verify(string password, string hash);
}

public interface ITokenGenerator
{
    (string Token, DateTime ExpiresAt) Generate(User user);
}

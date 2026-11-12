namespace Streakly.Application.DTOs;

public record RegisterRequest(string Email, string Password);
public record LoginRequest(string Email, string Password);
public record UserResponse(Guid Id, string Email, DateTime CreatedAt);
public record AuthResponse(string Token, DateTime ExpiresAt, UserResponse User);

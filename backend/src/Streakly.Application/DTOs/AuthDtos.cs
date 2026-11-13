namespace Streakly.Application.DTOs;
using Streakly.Domain.Enums;

public record RegisterRequest(string Email, string Password);
public record LoginRequest(string Email, string Password);
public record UserResponse(Guid Id, string Email, DateTime CreatedAt, UserRole Role);
public record AuthResponse(string Token, DateTime ExpiresAt, UserResponse User);

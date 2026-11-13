namespace Streakly.Domain.Entities;
using Streakly.Domain.Enums;
using Streakly.Domain.Exceptions;
public class User
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public string Email { get; private set; } = string.Empty;
    public string PasswordHash { get; private set; } = string.Empty;
    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;
    public UserRole Role { get; private set; } = UserRole.User;
    public DateTime? LastActiveAt { get; private set; }

    protected User() { }

    public User(string email, string passwordHash)
    {
        if (string.IsNullOrWhiteSpace(email) || !email.Contains("@"))
            throw new DomainException("El correo electrónico no es válido.");
        
        if (string.IsNullOrWhiteSpace(passwordHash))
            throw new DomainException("La contraseña no puede estar vacía.");
        
        Email = email.Trim().ToLowerInvariant();
        PasswordHash = passwordHash;
    }

    public void SetRole(UserRole role) => Role = role;

    public void MarkActive() => LastActiveAt = DateTime.UtcNow;
}
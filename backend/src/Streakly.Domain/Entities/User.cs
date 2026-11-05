namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;
public class User
{
    public Guid Id { get; } = Guid.NewGuid();
    public string Email { get; private set; } = string.Empty;
    public string PasswordHash { get; private set; } = string.Empty;
    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;

    protected User() { }

    public User(string email, string passwordHash)
    {
        if (string.IsNullOrWhiteSpace(email) || !email.Contains("@"))
            throw new DomainException("El correo electrónico no es válido.");
        
        if (string.IsNullOrWhiteSpace(passwordHash))
            throw new DomainException("La contraseña no puede estar vacía.");
        
        Email = email;
        PasswordHash = passwordHash;
    }
}
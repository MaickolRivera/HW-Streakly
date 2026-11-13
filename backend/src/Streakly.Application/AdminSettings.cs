namespace Streakly.Application;

/// <summary>
/// Correos que reciben el rol Admin al iniciar sesión o registrarse (config "Admin:Emails").
/// </summary>
public record AdminSettings(IReadOnlyCollection<string> Emails)
{
    public bool IsAdmin(string email) =>
        Emails.Any(e => string.Equals(e.Trim(), email, StringComparison.OrdinalIgnoreCase));
}

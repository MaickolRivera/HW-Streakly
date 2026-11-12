namespace Streakly.Infrastructure.Auth;

public class JwtOptions
{
    public const string SectionName = "Jwt";

    public string Issuer { get; set; } = "Streakly";
    public string Audience { get; set; } = "Streakly.App";
    /// <summary>
    /// Clave de firma HMAC. Nunca se guarda en el repositorio:
    /// se configura con user-secrets o con la variable de entorno Jwt__Key.
    /// </summary>
    public string Key { get; set; } = string.Empty;
    public int ExpirationDays { get; set; } = 30;
}

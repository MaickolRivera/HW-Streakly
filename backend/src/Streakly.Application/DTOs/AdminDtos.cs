namespace Streakly.Application.DTOs;

/// <summary>Usuario que usó la app dentro del periodo consultado.</summary>
/// <param name="Id">Identificador del usuario.</param>
/// <param name="Email">Correo del usuario.</param>
/// <param name="CreatedAt">Fecha de registro (UTC).</param>
/// <param name="LastActiveAt">Última vez que usó la API (UTC).</param>
public record ActiveUserResponse(Guid Id, string Email, DateTime CreatedAt, DateTime LastActiveAt);

namespace Streakly.Api.Middleware;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Streakly.Application.Exceptions;
using Streakly.Domain.Exceptions;

/// <summary>
/// Traduce las excepciones de dominio/aplicacion a respuestas ProblemDetails
/// con un mensaje legible para mostrar en la app.
/// </summary>
public class GlobalExceptionHandler : IExceptionHandler
{
    private readonly ILogger<GlobalExceptionHandler> _logger;
    private readonly IProblemDetailsService _problemDetails;

    public GlobalExceptionHandler(ILogger<GlobalExceptionHandler> logger, IProblemDetailsService problemDetails)
    {
        _logger = logger;
        _problemDetails = problemDetails;
    }

    public async ValueTask<bool> TryHandleAsync(HttpContext httpContext, Exception exception, CancellationToken cancellationToken)
    {
        var (status, title, detail) = exception switch
        {
            DomainException e => (StatusCodes.Status400BadRequest, "Solicitud inválida", e.Message),
            NotFoundException e => (StatusCodes.Status404NotFound, "No encontrado", e.Message),
            ConflictException e => (StatusCodes.Status409Conflict, "Conflicto", e.Message),
            UnauthorizedException e => (StatusCodes.Status401Unauthorized, "No autorizado", e.Message),
            DbUpdateException => (StatusCodes.Status409Conflict, "Conflicto", "Los datos entran en conflicto con un registro existente."),
            _ => (StatusCodes.Status500InternalServerError, "Error interno", "Ocurrió un error inesperado.")
        };

        if (status == StatusCodes.Status500InternalServerError)
            _logger.LogError(exception, "Error no controlado");

        httpContext.Response.StatusCode = status;
        return await _problemDetails.TryWriteAsync(new ProblemDetailsContext
        {
            HttpContext = httpContext,
            Exception = exception,
            ProblemDetails = new ProblemDetails { Status = status, Title = title, Detail = detail }
        });
    }
}

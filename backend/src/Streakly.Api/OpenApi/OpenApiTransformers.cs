namespace Streakly.Api.OpenApi;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

/// <summary>
/// Metadatos generales del documento y esquema de seguridad JWT Bearer.
/// </summary>
internal sealed class StreaklyDocumentTransformer : IOpenApiDocumentTransformer
{
    public const string BearerSchemeId = "Bearer";

    public Task TransformAsync(OpenApiDocument document, OpenApiDocumentTransformerContext context, CancellationToken cancellationToken)
    {
        document.Info = new OpenApiInfo
        {
            Title = "Streakly API",
            Version = "v1",
            Description = "API para gestionar hábitos, rachas y metas. " +
                          "Autentícate con POST /api/auth/login y usa el token en el botón **Authorize**."
        };

        document.Components ??= new OpenApiComponents();
        document.Components.SecuritySchemes ??= new Dictionary<string, IOpenApiSecurityScheme>();
        document.Components.SecuritySchemes[BearerSchemeId] = new OpenApiSecurityScheme
        {
            Type = SecuritySchemeType.Http,
            Scheme = "bearer",
            BearerFormat = "JWT",
            In = ParameterLocation.Header,
            Description = "Pega solo el token JWT (sin el prefijo 'Bearer')."
        };

        return Task.CompletedTask;
    }
}

/// <summary>
/// Marca como protegidos los endpoints con [Authorize] (respetando [AllowAnonymous])
/// para que Swagger UI envíe el token y documente el 401.
/// </summary>
internal sealed class AuthorizeOperationTransformer : IOpenApiOperationTransformer
{
    public Task TransformAsync(OpenApiOperation operation, OpenApiOperationTransformerContext context, CancellationToken cancellationToken)
    {
        var metadata = context.Description.ActionDescriptor.EndpointMetadata;
        var authorizeData = metadata.OfType<IAuthorizeData>().ToList();
        if (authorizeData.Count == 0 || metadata.OfType<IAllowAnonymous>().Any())
        {
            return Task.CompletedTask;
        }

        operation.Security ??= [];
        operation.Security.Add(new OpenApiSecurityRequirement
        {
            [new OpenApiSecuritySchemeReference(StreaklyDocumentTransformer.BearerSchemeId, context.Document)] = []
        });

        operation.Responses ??= new OpenApiResponses();
        operation.Responses.TryAdd("401", new OpenApiResponse { Description = "No autenticado o token inválido." });

        var roles = authorizeData.Where(a => !string.IsNullOrEmpty(a.Roles)).Select(a => a.Roles).ToList();
        if (roles.Count > 0)
        {
            operation.Responses.TryAdd("403", new OpenApiResponse { Description = $"Requiere rol: {string.Join(", ", roles)}." });
        }

        return Task.CompletedTask;
    }
}

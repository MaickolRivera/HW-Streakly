namespace Streakly.Application.Interfaces;
using Streakly.Application.DTOs;
using Streakly.Domain.Enums;

public interface IHabitService
{
    Task<HabitResponse> CreateAsync(Guid userId, CreateHabitRequest request);
    /// <param name="search">Texto a buscar en el nombre (opcional).</param>
    /// <param name="type">Tipo de habito (opcional).</param>
    Task<List<HabitResponse>> GetAllAsync(Guid userId, string? search = null, HabitType? type = null);
    Task<HabitResponse> UpdateAsync(Guid userId, Guid habitId, UpdateHabitRequest request);
    Task<HabitResponse> GetByIdAsync(Guid userId, Guid habitId);
    Task DeleteAsync(Guid userId, Guid habitId);

    Task<HabitEntryResponse> SetEntryAsync(Guid userId, Guid habitId, RegisterEntryRequest request);
    Task DeleteEntryAsync(Guid userId, Guid habitId, DateOnly date);

    Task<TodayResponse> GetTodayAsync(Guid userId, DateOnly today);
    Task<HabitStatsResponse> GetStatsAsync(Guid userId, Guid habitId, DateOnly today, int weeks);
    Task<List<HabitComplianceResponse>> GetComplianceAsync(Guid userId, DateOnly today, int days);
    Task<SummaryResponse> GetSummaryAsync(Guid userId, DateOnly today);
}

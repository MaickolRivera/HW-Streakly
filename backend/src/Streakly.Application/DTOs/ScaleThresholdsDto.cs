namespace Streakly.Application.DTOs;
public record ScaleThresholdsDto(decimal Minimum, decimal Good, decimal Excellent, decimal Maximum, string Unit);
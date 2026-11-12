namespace Streakly.Application.Services;

/// <summary>
/// Calculos de rachas independientes de la base de datos.
/// Reglas:
/// - Solo cuentan los dias programados (los demas se saltan, no rompen la racha).
/// - Si hoy esta programado y aun no se cumple, no rompe la racha: el dia sigue "pendiente".
/// - Un dia programado sin cumplir (antes de hoy) rompe la racha.
/// </summary>
public static class StreakCalculator
{
    public static int CurrentStreak(
        Func<DateOnly, bool> isScheduled,
        IReadOnlySet<DateOnly> completedDays,
        DateOnly start,
        DateOnly today)
    {
        var streak = 0;
        var day = today;

        if (isScheduled(today) && !completedDays.Contains(today))
            day = today.AddDays(-1);

        while (day >= start)
        {
            if (isScheduled(day))
            {
                if (!completedDays.Contains(day)) break;
                streak++;
            }
            day = day.AddDays(-1);
        }

        return streak;
    }

    public static int BestStreak(
        Func<DateOnly, bool> isScheduled,
        IReadOnlySet<DateOnly> completedDays,
        DateOnly start,
        DateOnly today)
    {
        var best = 0;
        var running = 0;

        for (var day = start; day <= today; day = day.AddDays(1))
        {
            if (!isScheduled(day)) continue;

            if (completedDays.Contains(day))
            {
                running++;
                best = Math.Max(best, running);
            }
            else if (day != today)
            {
                running = 0;
            }
        }

        return best;
    }

    /// <summary>
    /// Devuelve cuantos dias programados se cumplieron y cuantos habia en el rango [from, today].
    /// Hoy solo se cuenta si ya se cumplio.
    /// </summary>
    public static (int Completed, int Scheduled) Compliance(
        Func<DateOnly, bool> isScheduled,
        IReadOnlySet<DateOnly> completedDays,
        DateOnly from,
        DateOnly today)
    {
        var completed = 0;
        var scheduled = 0;

        for (var day = from; day <= today; day = day.AddDays(1))
        {
            if (!isScheduled(day)) continue;
            var done = completedDays.Contains(day);
            if (day == today && !done) continue;

            scheduled++;
            if (done) completed++;
        }

        return (completed, scheduled);
    }

    public static int Percent(int completed, int scheduled) =>
        scheduled == 0 ? 0 : (int)Math.Round(completed * 100.0 / scheduled);
}

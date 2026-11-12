namespace Streakly.Tests;
using Streakly.Application.Services;

public class StreakCalculatorTests
{
    // 2026-11-11 es miercoles
    private static readonly DateOnly Today = new(2026, 11, 11);
    private static readonly DateOnly Start = Today.AddDays(-60);
    private static bool EveryDay(DateOnly _) => true;
    private static bool Weekdays(DateOnly d) => d.DayOfWeek is not (DayOfWeek.Saturday or DayOfWeek.Sunday);

    private static HashSet<DateOnly> LastDays(int count, int offset = 0) =>
        Enumerable.Range(offset, count).Select(i => Today.AddDays(-i)).ToHashSet();

    [Fact]
    public void CurrentStreak_counts_consecutive_days_including_today()
    {
        var completed = LastDays(5);
        Assert.Equal(5, StreakCalculator.CurrentStreak(EveryDay, completed, Start, Today));
    }

    [Fact]
    public void CurrentStreak_does_not_break_when_today_is_still_pending()
    {
        var completed = LastDays(3, offset: 1); // ayer, antier, ...
        Assert.Equal(3, StreakCalculator.CurrentStreak(EveryDay, completed, Start, Today));
    }

    [Fact]
    public void CurrentStreak_breaks_on_a_missed_scheduled_day()
    {
        var completed = LastDays(2).Concat(LastDays(4, offset: 3)).ToHashSet(); // falta hace 2 dias
        Assert.Equal(2, StreakCalculator.CurrentStreak(EveryDay, completed, Start, Today));
    }

    [Fact]
    public void CurrentStreak_skips_unscheduled_days()
    {
        // lun 9, mar 10, mie 11 + vie 6, jue 5. Sabado y domingo no estan programados.
        var completed = new HashSet<DateOnly>
        {
            new(2026, 11, 11), new(2026, 11, 10), new(2026, 11, 9),
            new(2026, 11, 6), new(2026, 11, 5)
        };
        Assert.Equal(5, StreakCalculator.CurrentStreak(Weekdays, completed, Start, Today));
    }

    [Fact]
    public void BestStreak_finds_longest_run()
    {
        var completed = LastDays(3).Concat(LastDays(7, offset: 10)).ToHashSet();
        Assert.Equal(7, StreakCalculator.BestStreak(EveryDay, completed, Start, Today));
    }

    [Fact]
    public void BestStreak_is_zero_without_entries()
    {
        Assert.Equal(0, StreakCalculator.BestStreak(EveryDay, new HashSet<DateOnly>(), Start, Today));
    }

    [Fact]
    public void Compliance_ignores_today_when_pending()
    {
        var from = Today.AddDays(-9); // 10 dias contando hoy
        var completed = LastDays(5, offset: 1);
        var (done, scheduled) = StreakCalculator.Compliance(EveryDay, completed, from, Today);
        Assert.Equal(5, done);
        Assert.Equal(9, scheduled);
    }

    [Fact]
    public void Percent_rounds_and_handles_zero()
    {
        Assert.Equal(80, StreakCalculator.Percent(4, 5));
        Assert.Equal(0, StreakCalculator.Percent(0, 0));
    }
}

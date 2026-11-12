namespace Streakly.Domain.Enums;

[Flags]
public enum WeekDays
{
    None = 0,
    Monday = 1,
    Tuesday = 2,
    Wednesday = 4,
    Thursday = 8,
    Friday = 16,
    Saturday = 32,
    Sunday = 64,
    All = Monday | Tuesday | Wednesday | Thursday | Friday | Saturday | Sunday
}

public static class WeekDaysExtensions
{
    public static WeekDays ToWeekDay(this DayOfWeek day) => day switch
    {
        DayOfWeek.Monday => WeekDays.Monday,
        DayOfWeek.Tuesday => WeekDays.Tuesday,
        DayOfWeek.Wednesday => WeekDays.Wednesday,
        DayOfWeek.Thursday => WeekDays.Thursday,
        DayOfWeek.Friday => WeekDays.Friday,
        DayOfWeek.Saturday => WeekDays.Saturday,
        _ => WeekDays.Sunday
    };

    // ISO: 1 = lunes ... 7 = domingo
    public static WeekDays FromIsoDays(IEnumerable<int> days)
    {
        var result = WeekDays.None;
        foreach (var d in days)
        {
            if (d < 1 || d > 7)
                throw new ArgumentOutOfRangeException(nameof(days), "Los días deben estar entre 1 (lunes) y 7 (domingo).");
            result |= (WeekDays)(1 << (d - 1));
        }
        return result;
    }

    public static List<int> ToIsoDays(this WeekDays days) =>
        Enumerable.Range(1, 7).Where(d => days.HasFlag((WeekDays)(1 << (d - 1)))).ToList();
}

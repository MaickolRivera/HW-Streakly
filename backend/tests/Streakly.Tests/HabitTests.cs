namespace Streakly.Tests;
using Streakly.Domain.Entities;
using Streakly.Domain.Enums;
using Streakly.Domain.Exceptions;

public class HabitTests
{
    private static Habit Counter(decimal target = 2, WeekDays days = WeekDays.All) =>
        new(Guid.NewGuid(), "Agua", "drop", CategoryType.Nutrition, HabitType.Counter, target, "botellones", days, null, null);

    private static Habit Boolean() =>
        new(Guid.NewGuid(), "Programar", "code", CategoryType.Productivity, HabitType.Boolean, 30, "min", WeekDays.All, null, null);

    [Fact]
    public void Counter_is_completed_only_when_reaching_target()
    {
        var habit = Counter(target: 2);
        Assert.False(habit.IsCompletedWith(1));
        Assert.True(habit.IsCompletedWith(2));
        Assert.True(habit.IsCompletedWith(3));
    }

    [Fact]
    public void Boolean_entry_must_be_one()
    {
        var habit = Boolean();
        Assert.Throws<DomainException>(() => habit.RegisterEntry(new DateOnly(2026, 1, 1), 2));
        Assert.Equal(1, habit.RegisterEntry(new DateOnly(2026, 1, 1), 1).Value);
    }

    [Fact]
    public void Entries_in_the_future_are_rejected()
    {
        var habit = Boolean();
        var future = DateOnly.FromDateTime(DateTime.UtcNow).AddDays(5);
        Assert.Throws<DomainException>(() => habit.RegisterEntry(future, 1));
    }

    [Fact]
    public void Habit_requires_at_least_one_day()
    {
        Assert.Throws<DomainException>(() => Counter(days: WeekDays.None));
    }

    [Fact]
    public void IsScheduledOn_respects_selected_days()
    {
        var habit = Counter(days: WeekDays.Monday | WeekDays.Wednesday);
        Assert.True(habit.IsScheduledOn(new DateOnly(2026, 11, 11)));  // miercoles
        Assert.False(habit.IsScheduledOn(new DateOnly(2026, 11, 12))); // jueves
    }

    [Fact]
    public void Iso_days_round_trip()
    {
        var days = WeekDaysExtensions.FromIsoDays([1, 3, 7]);
        Assert.Equal(WeekDays.Monday | WeekDays.Wednesday | WeekDays.Sunday, days);
        Assert.Equal([1, 3, 7], days.ToIsoDays());
    }

    [Fact]
    public void Goal_toggle_task_flips_state()
    {
        var goal = new Goal(Guid.NewGuid(), "Entregar la app");
        var task = goal.AddTask("Backend con login");
        goal.ToggleTask(task.Id);
        Assert.Equal(1, goal.CompletedTasks);
        goal.ToggleTask(task.Id);
        Assert.Equal(0, goal.CompletedTasks);
    }
}

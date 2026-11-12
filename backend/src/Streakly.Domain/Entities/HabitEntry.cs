namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;
public class HabitEntry
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid HabitId { get; private set; }
    public DateOnly EntryDate { get; private set; }
    public decimal Value { get; private set; }

    protected HabitEntry() { }

    public HabitEntry(Guid habitId, DateOnly date, decimal value)
    {
        if (value < 0)
        {
            throw new DomainException("El valor del hábito no puede ser negativo.");
        }

        HabitId = habitId;
        EntryDate = date;
        Value = value;
    }

    internal void UpdateValue(decimal value)
    {
        if (value < 0)
            throw new DomainException("El valor del hábito no puede ser negativo.");
        Value = value;
    }
}

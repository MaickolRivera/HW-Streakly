namespace Streakly.Domain.Entities;
using Streakly.Domain.Enums;
using Streakly.Domain.ValueObjects;
using Streakly.Domain.Exceptions;
public class Habit
{
    public Guid Id { get; } = Guid.NewGuid();
    public Guid UserId { get; private set; }
    public string Name { get; private set;} = string.Empty;
    public CategoryType Category { get; private set;}
    public HabitType Type { get; private set; }
    public ScaleThresholds? Thresholds  { get; private set; }

    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;

    protected Habit(){}

    public Habit (Guid userId, string name, CategoryType category, HabitType type, ScaleThresholds? thresholds)
    {
        if (string.IsNullOrWhiteSpace(name))
            throw new DomainException("El nombre del hábito no puede estar vacío.");
        if (type == HabitType.Scale && thresholds == null)
            throw new DomainException("Los valores de la escala son obligatorios para los hábitos de tipo escala.");
        if (type != HabitType.Scale && thresholds != null)
            throw new DomainException("Los valores de la escala solo son válidos para los hábitos de tipo escala.");

        UserId = userId;
        Name = name;
        Category = category;
        Type = type;
        Thresholds = thresholds;
    }

    public HabitEntry RegisterEntry(DateOnly date, decimal value)
    {
        if(date > DateOnly.FromDateTime(DateTime.UtcNow))
            throw new DomainException("La fecha de la entrada no puede ser futura.");

        switch (Type)
        {
            case HabitType.Boolean:
                if (value != 1)
                    throw new DomainException("El valor del hábito booleano debe ser 1.");
                break;

            case HabitType.Counter:
                if (value <= 0)
                    throw new DomainException("El valor del hábito contador no puede ser negativo.");
                break;

            case HabitType.Scale:
                if (value != Math.Floor(value) && value < 1 || value > 4)
                    throw new DomainException("El valor de la escala debe estar entre 1 y 4.");
                break;
        }

        return new HabitEntry(Id, date, value);

    }

}
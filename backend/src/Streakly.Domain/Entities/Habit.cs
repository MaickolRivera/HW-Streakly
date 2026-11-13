namespace Streakly.Domain.Entities;
using Streakly.Domain.Enums;
using Streakly.Domain.ValueObjects;
using Streakly.Domain.Exceptions;
public class Habit
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid UserId { get; private set; }
    public string Name { get; private set; } = string.Empty;
    public string Icon { get; private set; } = "spark";
    public CategoryType Category { get; private set; }
    public HabitType Type { get; private set; }
    public decimal TargetValue { get; private set; } = 1;
    public string Unit { get; private set; } = string.Empty;
    public WeekDays ScheduledDays { get; private set; } = WeekDays.All;
    public TimeOnly? ReminderTime { get; private set; }
    public ScaleThresholds? Thresholds { get; private set; }

    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;

    protected Habit() { }

    public Habit(
        Guid userId,
        string name,
        string icon,
        CategoryType category,
        HabitType type,
        decimal targetValue,
        string unit,
        WeekDays scheduledDays,
        TimeOnly? reminderTime,
        ScaleThresholds? thresholds)
    {
        if (string.IsNullOrWhiteSpace(name))
            throw new DomainException("El nombre del hábito no puede estar vacío.");
        if (name.Trim().Length > 60)
            throw new DomainException("El nombre del hábito no puede superar 60 caracteres.");
        if (type == HabitType.Scale && thresholds == null)
            throw new DomainException("Los valores de la escala son obligatorios para los hábitos de tipo escala.");
        if (type != HabitType.Scale && thresholds != null)
            throw new DomainException("Los valores de la escala solo son válidos para los hábitos de tipo escala.");
        if (targetValue <= 0)
            throw new DomainException("El mínimo diario debe ser mayor que 0.");
        if (type == HabitType.Counter && targetValue != Math.Floor(targetValue))
            throw new DomainException("El mínimo de un hábito contador debe ser un número entero.");
        if (scheduledDays == WeekDays.None)
            throw new DomainException("Selecciona al menos un día para el hábito.");

        UserId = userId;
        Name = name.Trim();
        Icon = string.IsNullOrWhiteSpace(icon) ? "spark" : icon.Trim();
        Category = category;
        Type = type;
        TargetValue = targetValue;
        Unit = unit?.Trim() ?? string.Empty;
        ScheduledDays = scheduledDays;
        ReminderTime = reminderTime;
        Thresholds = thresholds;
    }

    /// <summary>
    /// Edita los datos del habito. El tipo no se puede cambiar porque invalidaria
    /// el historial de registros (p. ej. un contador con valor 2 no es un "hecho").
    /// </summary>
    public void Update(string name, string icon, decimal targetValue, string unit, WeekDays scheduledDays, TimeOnly? reminderTime)
    {
        if (string.IsNullOrWhiteSpace(name))
            throw new DomainException("El nombre del hábito no puede estar vacío.");
        if (name.Trim().Length > 60)
            throw new DomainException("El nombre del hábito no puede superar 60 caracteres.");
        if (targetValue <= 0)
            throw new DomainException("El mínimo diario debe ser mayor que 0.");
        if (Type == HabitType.Counter && targetValue != Math.Floor(targetValue))
            throw new DomainException("El mínimo de un hábito contador debe ser un número entero.");
        if (scheduledDays == WeekDays.None)
            throw new DomainException("Selecciona al menos un día para el hábito.");

        Name = name.Trim();
        Icon = string.IsNullOrWhiteSpace(icon) ? Icon : icon.Trim();
        TargetValue = targetValue;
        Unit = unit?.Trim() ?? string.Empty;
        ScheduledDays = scheduledDays;
        ReminderTime = reminderTime;
    }

    public bool IsScheduledOn(DateOnly date) => ScheduledDays.HasFlag(date.DayOfWeek.ToWeekDay());

    /// <summary>
    /// Indica si el valor registrado en un día alcanza el mínimo y cuenta para la racha.
    /// </summary>
    public bool IsCompletedWith(decimal value) => Type switch
    {
        HabitType.Boolean => value >= 1,
        HabitType.Counter => value >= TargetValue,
        HabitType.Scale => value >= (int)ScaleLevel.Minimum,
        _ => false
    };

    public HabitEntry RegisterEntry(DateOnly date, decimal value)
    {
        ValidateEntry(date, value);
        return new HabitEntry(Id, date, value);
    }

    public void UpdateEntry(HabitEntry entry, decimal value)
    {
        if (entry.HabitId != Id)
            throw new DomainException("La entrada no pertenece a este hábito.");
        ValidateEntry(entry.EntryDate, value);
        entry.UpdateValue(value);
    }

    private void ValidateEntry(DateOnly date, decimal value)
    {
        // Se tolera un día por diferencias de zona horaria entre cliente y servidor.
        if (date > DateOnly.FromDateTime(DateTime.UtcNow).AddDays(1))
            throw new DomainException("La fecha de la entrada no puede ser futura.");

        switch (Type)
        {
            case HabitType.Boolean:
                if (value != 1)
                    throw new DomainException("El valor del hábito booleano debe ser 1.");
                break;

            case HabitType.Counter:
                if (value <= 0 || value != Math.Floor(value))
                    throw new DomainException("El valor del hábito contador debe ser un entero mayor que 0.");
                break;

            case HabitType.Scale:
                if (value != Math.Floor(value) || value < 1 || value > 4)
                    throw new DomainException("El valor de la escala debe estar entre 1 y 4.");
                break;
        }
    }
}

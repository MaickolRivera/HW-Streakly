namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;
public class GoalTask
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid GoalId { get; private set; }
    public string Title { get; private set; } = string.Empty;
    public bool IsCompleted { get; private set; } = false;
    public int Position { get; private set; }

    protected GoalTask() { }

    public GoalTask(Guid goalId, string title, int position = 0)
    {
        if (string.IsNullOrWhiteSpace(title))
            throw new DomainException("El título no puede estar vacío.");
        if (title.Trim().Length > 200)
            throw new DomainException("El título no puede superar 200 caracteres.");

        GoalId = goalId;
        Title = title.Trim();
        IsCompleted = false;
        Position = position;
    }

    public void CompleteTask()
    {
        IsCompleted = true;
    }

    public void Toggle()
    {
        IsCompleted = !IsCompleted;
    }

    public void UpdateTitle(string title)
    {
        if (string.IsNullOrWhiteSpace(title))
        {
            throw new DomainException("El título no puede estar vacío.");
        }

        Title = title.Trim();
    }
}

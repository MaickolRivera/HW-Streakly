namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;
public class GoalTask
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid GoalId { get; private set; }
    public string Title { get; private set; } = string.Empty;
    public bool IsCompleted { get; private set; } = false;

    protected GoalTask() { }

    public GoalTask(Guid goalId, string title)
    {
        if (string.IsNullOrWhiteSpace(title))
            throw new DomainException("El título no puede estar vacío.");

        GoalId = goalId;
        Title = title;
        IsCompleted = false;
    }

    public void CompleteTask()
    {
        IsCompleted = true;
    }

    public void UpdateTitle(string title)
    {
        if (string.IsNullOrWhiteSpace(title))
        {
            throw new DomainException("El título no puede estar vacío.");
        }

        Title = title;
    }
}
namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;

public class Goal
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid UserId { get; private set; }
    public string Title { get; private set; } = string.Empty;
    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;
    private readonly List<GoalTask> _tasks = new List<GoalTask>();
    public IReadOnlyCollection<GoalTask> Tasks => _tasks.AsReadOnly();

    protected Goal() { }

    public Goal(Guid userId, string title)
    {

        if (string.IsNullOrWhiteSpace(title))
            throw new DomainException("El título no puede estar vacío.");

        UserId = userId;
        Title = title;
    }

    public void CompleteTask(Guid taskId)
    {
        var task = _tasks.FirstOrDefault(t => t.Id == taskId);
        if (task == null)
        {
            throw new DomainException("La tarea no existe.");
        }
        task.CompleteTask();
    }

    public void AddTask(string title)
    {
        var task = new GoalTask(Id, title);
        _tasks.Add(task);
    }
}
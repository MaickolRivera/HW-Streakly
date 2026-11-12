namespace Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;

public class Goal
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid UserId { get; private set; }
    public string Title { get; private set; } = string.Empty;
    public string? Description { get; private set; }
    public DateOnly? DueDate { get; private set; }
    public DateTime CreatedAt { get; private set; } = DateTime.UtcNow;
    private readonly List<GoalTask> _tasks = new List<GoalTask>();
    public IReadOnlyCollection<GoalTask> Tasks => _tasks.AsReadOnly();

    protected Goal() { }

    public Goal(Guid userId, string title, string? description = null, DateOnly? dueDate = null)
    {
        if (string.IsNullOrWhiteSpace(title))
            throw new DomainException("El título no puede estar vacío.");
        if (title.Trim().Length > 200)
            throw new DomainException("El título no puede superar 200 caracteres.");
        if (description?.Length > 1000)
            throw new DomainException("La descripción no puede superar 1000 caracteres.");

        UserId = userId;
        Title = title.Trim();
        Description = string.IsNullOrWhiteSpace(description) ? null : description.Trim();
        DueDate = dueDate;
    }

    public int CompletedTasks => _tasks.Count(t => t.IsCompleted);

    public GoalTask AddTask(string title)
    {
        var position = _tasks.Count == 0 ? 0 : _tasks.Max(t => t.Position) + 1;
        var task = new GoalTask(Id, title, position);
        _tasks.Add(task);
        return task;
    }

    public void CompleteTask(Guid taskId) => FindTask(taskId).CompleteTask();

    public void ToggleTask(Guid taskId) => FindTask(taskId).Toggle();

    public void RemoveTask(Guid taskId) => _tasks.Remove(FindTask(taskId));

    private GoalTask FindTask(Guid taskId)
    {
        var task = _tasks.FirstOrDefault(t => t.Id == taskId);
        if (task == null)
        {
            throw new DomainException("La tarea no existe.");
        }
        return task;
    }
}

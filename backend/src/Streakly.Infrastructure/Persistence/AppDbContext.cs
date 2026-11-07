namespace Streakly.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Streakly.Domain.Entities;
public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options): base(options)
    {
        
    }
    public DbSet<Goal> Goals => Set<Goal>();
    public DbSet<GoalTask> GoalTasks => Set<GoalTask>();
    public DbSet<Habit> Habits => Set<Habit>();
    public DbSet<HabitEntry> HabitEntries => Set<HabitEntry>();
    public DbSet<User> Users => Set<User>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(AppDbContext).Assembly);
    }

}
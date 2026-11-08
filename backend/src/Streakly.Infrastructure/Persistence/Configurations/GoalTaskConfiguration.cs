namespace Streakly.Infrastructure.Persistence.Configurations;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Streakly.Domain.Entities;

public class GoalTaskConfiguration : IEntityTypeConfiguration<GoalTask>
{
    public void Configure(EntityTypeBuilder<GoalTask> builder)
    {
        builder.ToTable("goal_tasks");
        builder.HasKey(t => t.Id);
        builder.Property(g => g.Id).ValueGeneratedNever();
        builder.Property(t => t.Title).IsRequired().HasMaxLength(200);
    }
}
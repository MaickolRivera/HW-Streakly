namespace Streakly.Infrastructure.Persistence.Configurations;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Streakly.Domain.Entities;

public class GoalConfiguration : IEntityTypeConfiguration<Goal>
{
    public void Configure(EntityTypeBuilder<Goal> builder)
    {
        builder.ToTable("goals");
        builder.HasKey(g => g.Id);
        builder.Property(g => g.Id).ValueGeneratedNever();
        builder.Property(g => g.Title).IsRequired().HasMaxLength(200);
        builder.Property(g => g.Description).HasMaxLength(1000);
        builder.HasIndex(g => g.UserId);
        builder.HasOne<User>().WithMany().HasForeignKey(g => g.UserId).OnDelete(DeleteBehavior.Cascade);
        builder.Ignore(g => g.CompletedTasks);

        builder.HasMany(g => g.Tasks).WithOne().HasForeignKey(t => t.GoalId).OnDelete(DeleteBehavior.Cascade);
        builder.Navigation(g => g.Tasks).UsePropertyAccessMode(PropertyAccessMode.Field);
    }
}

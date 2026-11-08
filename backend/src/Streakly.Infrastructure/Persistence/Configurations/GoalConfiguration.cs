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
        builder.Property(g => g.Title).IsRequired().HasMaxLength(200);

        builder.HasMany(g => g.Tasks).WithOne().HasForeignKey(t=>t.GoalId).OnDelete(DeleteBehavior.Cascade);
        builder.Property(g => g.Id).ValueGeneratedNever();
        builder.Navigation(g=> g.Tasks).UsePropertyAccessMode(PropertyAccessMode.Field);
    }
}
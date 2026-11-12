namespace Streakly.Infrastructure.Persistence.Configurations;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Streakly.Domain.Entities;

public class HabitEntryConfiguration : IEntityTypeConfiguration<HabitEntry>
{
    public void Configure(EntityTypeBuilder<HabitEntry> builder)
    {
        builder.ToTable("habit_entries");
        builder.HasKey(e => e.Id);
        builder.Property(e => e.Id).ValueGeneratedNever();
        builder.Property(e => e.Value).HasPrecision(10, 2);
        // Una sola entrada por habito y dia.
        builder.HasIndex(e => new { e.HabitId, e.EntryDate }).IsUnique();
        builder.HasOne<Habit>().WithMany().HasForeignKey(e => e.HabitId).OnDelete(DeleteBehavior.Cascade);
    }
}

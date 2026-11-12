namespace Streakly.Infrastructure.Persistence.Configurations;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Streakly.Domain.Entities;

public class HabitConfiguration : IEntityTypeConfiguration<Habit>
{
    public void Configure(EntityTypeBuilder<Habit> builder)
    {
        builder.ToTable("habits");
        builder.HasKey(h => h.Id);
        builder.Property(h => h.Id).ValueGeneratedNever();
        builder.Property(h => h.Name).IsRequired().HasMaxLength(60);
        builder.Property(h => h.Icon).IsRequired().HasMaxLength(30);
        builder.Property(h => h.Unit).IsRequired().HasMaxLength(30);
        builder.Property(h => h.TargetValue).HasPrecision(10, 2);
        builder.Property(h => h.Category).HasConversion<string>().HasMaxLength(30);
        builder.Property(h => h.Type).HasConversion<string>().HasMaxLength(20);
        builder.Property(h => h.ScheduledDays).HasConversion<int>();
        builder.HasIndex(h => new { h.UserId, h.Name }).IsUnique();
        builder.HasOne<User>().WithMany().HasForeignKey(h => h.UserId).OnDelete(DeleteBehavior.Cascade);

        builder.OwnsOne(h => h.Thresholds, t =>
        {
            t.Property(x => x.Minimum).HasColumnName("scale_minimum").HasPrecision(10, 2);
            t.Property(x => x.Good).HasColumnName("scale_good").HasPrecision(10, 2);
            t.Property(x => x.Excellent).HasColumnName("scale_excellent").HasPrecision(10, 2);
            t.Property(x => x.Maximum).HasColumnName("scale_maximum").HasPrecision(10, 2);
            t.Property(x => x.Unit).HasColumnName("scale_unit").HasMaxLength(30);
        });
    }
}

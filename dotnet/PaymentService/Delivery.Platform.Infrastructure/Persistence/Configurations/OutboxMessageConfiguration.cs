using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace Delivery.Platform.Infrastructure.Persistence.Configurations;

public sealed class OutboxMessageConfiguration
    : IEntityTypeConfiguration<OutboxMessage>
{
    public void Configure(EntityTypeBuilder<OutboxMessage> builder)
    {
        builder.ToTable(
            "outbox_messages",
            table =>
            {
                table.HasCheckConstraint(
                    "ck_outbox_status",
                    "status IN ('PENDING', 'PUBLISHED', 'DEAD')");

                table.HasCheckConstraint(
                    "ck_outbox_attempts",
                    "attempts >= 0");
            });

        builder.HasKey(x => x.Id);

        builder.Property(x => x.Id)
            .HasColumnName("id");

        builder.Property(x => x.AggregateId)
            .HasColumnName("aggregate_id")
            .IsRequired();

        builder.Property(x => x.Module)
            .HasColumnName("module")
            .HasMaxLength(100)
            .IsRequired();

        builder.Property(x => x.EventId)
            .HasColumnName("event_id")
            .IsRequired();

        builder.Property(x => x.EventType)
            .HasColumnName("event_type")
            .HasMaxLength(150)
            .IsRequired();

        builder.Property(x => x.Payload)
            .HasColumnName("payload")
            .HasColumnType("jsonb")
            .IsRequired();

        builder.Property(x => x.Status)
            .HasColumnName("status")
            .HasConversion(new OutboxMessageStatusConverter())
            .HasMaxLength(30)
            .IsRequired();

        builder.Property(x => x.Attempts)
            .HasColumnName("attempts")
            .HasDefaultValue(0)
            .IsRequired();

        builder.Property(x => x.NextAttemptAt)
            .HasColumnName("next_attempt_at")
            .IsRequired();

        builder.Property(x => x.OccurredAt)
            .HasColumnName("occurred_at")
            .IsRequired();

        builder.Property(x => x.CreatedAt)
            .HasColumnName("created_at")
            .IsRequired();

        builder.Property(x => x.PublishedAt)
            .HasColumnName("published_at");

        builder.Property(x => x.LastError)
            .HasColumnName("last_error");

        builder.Property(x => x.LockedUntil)
            .HasColumnName("locked_until");

        builder.HasIndex(x => new
            {
                x.NextAttemptAt,
                x.CreatedAt,
            })
            .HasDatabaseName("idx_outbox_pending")
            .HasFilter("status = 'PENDING'");

        builder.HasIndex(x => new
            {
                x.Module,
                x.AggregateId,
            })
            .HasDatabaseName("idx_outbox_aggregate");
    }
}
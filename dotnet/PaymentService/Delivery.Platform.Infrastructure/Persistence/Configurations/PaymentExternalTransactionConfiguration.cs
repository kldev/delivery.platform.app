using Delivery.Platform.Domain.Payments;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace Delivery.Platform.Infrastructure.Persistence.Configurations;

public class PaymentExternalTransactionEntityConfiguration
    : IEntityTypeConfiguration<PaymentExternalTransaction>
{
    public void Configure(
        EntityTypeBuilder<PaymentExternalTransaction> builder)
    {
        builder.ToTable("payment_external_transactions");

        builder.HasKey(x => x.Id);

        builder.Property(x => x.Id)
            .ValueGeneratedNever();

        // builder.Property(x => x.PaymentId)
        //     .IsRequired();

        builder.Property(x => x.ExternalTransactionId)
            .HasMaxLength(255)
            .IsRequired();

        builder.Property(x => x.Provider)
            .HasMaxLength(100)
            .IsRequired();

        builder.Property(x => x.CreatedAt)
            .IsRequired();

        builder.HasOne<Payment>()
            .WithMany()
            .HasForeignKey(x => x.PaymentId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(x => x.PaymentId);

        builder.HasIndex(x => new
            {
                x.Provider,
                x.ExternalTransactionId
            })
            .IsUnique();

        builder.HasIndex(x => x.ExternalTransactionId);
    }
}
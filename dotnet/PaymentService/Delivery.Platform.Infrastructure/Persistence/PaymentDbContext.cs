using Delivery.Platform.Domain.Payments;
using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.PaymentService.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Delivery.Platform.Infrastructure.Persistence;

public class PaymentDbContext(
    DbContextOptions<PaymentDbContext> options)
    : DbContext(options)
{
    public DbSet<Payment> Payments => Set<Payment>();
    public DbSet<PaymentExternalTransaction> External => Set<PaymentExternalTransaction>();
    
    public DbSet<ProcessedEvent> ProcessedEvents => Set<ProcessedEvent>();

    public DbSet<OutboxMessage> OutboxMessages => Set<OutboxMessage>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(
            typeof(PaymentDbContext).Assembly);
    }
}
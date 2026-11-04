using Delivery.Platform.Domain.Payments;
using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.PaymentService.Events;
using Delivery.Platform.PaymentService.Events.Contracts;
using Delivery.Platform.PaymentService.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace Delivery.Platform.PaymentService.Application.Payments.ProcessDeliveryConfirmed;

public sealed class ProcessDeliveryConfirmedHandler(
    PaymentDbContext dbContext,
    ILogger<ProcessDeliveryConfirmedHandler> logger)
{
    public async Task Handle(
        DeliveryConfirmed @event,
        CancellationToken cancellationToken = default)
    {
        logger.LogInformation(
            "Processing {EventType} event {EventId} for delivery {DeliveryId}",
            @event.EventType,
            @event.EventId,
            @event.DeliveryId);

        var errors = new List<String>();

        if (string.IsNullOrEmpty(@event.Currency))
        {
            logger.LogError("Invalid currency {Currency}", @event.Currency);
            errors.Add($"Invalid currency {@event.Currency}");
        }
        
        if (@event.DeliveryId == Guid.Empty)
        {
            logger.LogError("Invalid delivery {DeliveryId}", @event.DeliveryId);
            errors.Add($"Invalid delivery {@event.DeliveryId}");
        }

        if (errors.Count != 0)
        {
            throw new ValidationException(string.Join(",", errors));
        }
        await using var transaction =
            await dbContext.Database.BeginTransactionAsync(
                cancellationToken);

        var inserted = await dbContext.Database.ExecuteSqlInterpolatedAsync(
            $"""
             INSERT INTO processed_events (
                 event_id,
                 event_type,
                 processed_at
             )
             VALUES (
                 {@event.EventId},
                 {@event.EventType},
                 {DateTimeOffset.UtcNow}
             )
             ON CONFLICT (event_id) DO NOTHING
             """,
            cancellationToken);

        if (inserted == 0)
        {
            logger.LogInformation(
                "Event {EventId} has already been processed. Skipping delivery {DeliveryId}",
                @event.EventId,
                @event.DeliveryId);

            await transaction.CommitAsync(cancellationToken);
            return;
        }

        var payment = new Payment(
            Guid.NewGuid(),
            @event.DeliveryId,
            @event.Price,
            @event.Currency);

        dbContext.Payments.Add(payment);

        await dbContext.SaveChangesAsync(cancellationToken);

        await transaction.CommitAsync(cancellationToken);

        logger.LogInformation(
            "Payment {PaymentId} created for delivery {DeliveryId}. Amount: {Amount} {Currency}",
            payment.Id,
            @event.DeliveryId,
            @event.Price,
            @event.Currency);
    }
}
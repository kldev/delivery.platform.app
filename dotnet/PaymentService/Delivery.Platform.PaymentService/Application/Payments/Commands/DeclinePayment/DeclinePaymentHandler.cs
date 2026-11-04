using System.Text.Json;
using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.PaymentService.Events.Contracts;

namespace Delivery.Platform.PaymentService.Application.Payments.Commands.DeclinePayment;

public sealed class DeclinePaymentHandler(PaymentDbContext dbContext, JsonSerializerOptions jsonOptions)
{
    public async Task Handle(
        DeclinePaymentCommand command,
        CancellationToken cancellationToken)
    {
        var payment = await dbContext.Payments
            .FindAsync([command.PaymentId], cancellationToken);

        if (payment is null)
            throw new InvalidOperationException(
                $"Payment '{command.PaymentId}' was not found.");

        payment.MarkAsFailed();
        
        
        var @event = new PaymentDeclinedEvent(
            EventId: Guid.NewGuid(),
            PaymentId: command.PaymentId,
            DeliveryId: payment.DeliveryId,
            OccurredAt: DateTimeOffset.Now,
            AggregateId: payment.Id);

        var outbox = OutboxMessage.Create(@event, 
            JsonSerializer.Serialize(@event, jsonOptions));
        await dbContext.OutboxMessages.AddAsync(outbox, cancellationToken);
        
        await dbContext.SaveChangesAsync(cancellationToken);
    }
}
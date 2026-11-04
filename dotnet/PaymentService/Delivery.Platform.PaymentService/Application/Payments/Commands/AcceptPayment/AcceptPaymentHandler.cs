using System.Text.Json;
using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.PaymentService.Events.Contracts;

namespace Delivery.Platform.PaymentService.Application.Payments.Commands.AcceptPayment;

public sealed class AcceptPaymentHandler(
    PaymentDbContext dbContext, JsonSerializerOptions jsonOptions)
{
    public async Task Handle(
        AcceptPaymentCommand command,
        CancellationToken cancellationToken)
    {
        var payment = await dbContext.Payments
            .FindAsync([command.PaymentId], cancellationToken);

        if (payment is null)
            throw new InvalidOperationException(
                $"Payment '{command.PaymentId}' was not found.");

        payment.MarkAsPaid();

        var @event = new PaymentPaidEvent(
            EventId: Guid.NewGuid(),
            PaymentId: payment.Id,
            DeliveryId: payment.DeliveryId,
            OccurredAt: DateTimeOffset.Now,
            AggregateId: payment.Id);

        var message = OutboxMessage.Create(
            @event,
            JsonSerializer.Serialize(@event, jsonOptions));

        await dbContext.OutboxMessages.AddAsync(message, cancellationToken);

        await dbContext.SaveChangesAsync(cancellationToken);
    }
}
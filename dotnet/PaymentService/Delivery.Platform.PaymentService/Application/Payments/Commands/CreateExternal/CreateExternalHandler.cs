using System.Text.Json;
using Delivery.Platform.Domain.Payments;
using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.PaymentService.Events.Contracts;

namespace Delivery.Platform.PaymentService.Application.Payments.Commands.CreateExternal;

public sealed class CreateExternalHandler(PaymentDbContext dbContext, JsonSerializerOptions jsonOptions)
{
    public async Task<Resource> Handle(
        CreateExternalCommand command,
        CancellationToken cancellationToken)
    {
        var payment = await dbContext.Payments
            .FindAsync([command.PaymentId], cancellationToken);

        if (payment is null)
            throw new InvalidOperationException(
                $"Payment '{command.PaymentId}' was not found.");

        var id = Guid.NewGuid();
        await dbContext.External.AddAsync(
            new PaymentExternalTransaction(id,
                payment.Id,
                command.TransactionId,
                command.Provider, 
                command.Amount), cancellationToken);

        var @event = PaymentCompletedEvent.Create(payment.DeliveryId,
            payment.Id, command.TransactionId, command.Amount, payment.Currency);

        var outbox = OutboxMessage.Create(@event,
            JsonSerializer.Serialize(@event, jsonOptions));
        
        await dbContext.OutboxMessages.AddAsync(outbox, cancellationToken);
        
        await dbContext.SaveChangesAsync(cancellationToken);

        return new Resource(id);
    }

}
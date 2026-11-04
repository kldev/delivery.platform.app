using Delivery.Platform.PaymentService.Application.Payments.ProcessDeliveryConfirmed;
using Delivery.Platform.PaymentService.Events.Contracts;
using Delivery.Platform.PaymentService.Messaging;

namespace Delivery.Platform.PaymentService.Events.Handlers;

// ReSharper disable once ClassNeverInstantiated.Global
public sealed class DeliveryConfirmedHandler
    (ILogger<DeliveryConfirmedHandler> logger, 
        ProcessDeliveryConfirmedHandler processDeliveryConfirmedHandler) : IEventHandler<DeliveryConfirmed>
{
    public async Task HandleAsync(DeliveryConfirmed @event, CancellationToken cancellationToken)
    {
        logger.LogInformation(
            "Processing delivery confirmed event. DeliveryId: {DeliveryId}, Price: {Price}",
            @event.DeliveryId,
            @event.Price);
        
        await processDeliveryConfirmedHandler.Handle(@event, cancellationToken);
    }
}
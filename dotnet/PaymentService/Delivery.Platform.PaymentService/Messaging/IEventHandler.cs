namespace Delivery.Platform.PaymentService.Messaging;

public interface IEventHandler<in TEvent>
{
    Task HandleAsync(
        TEvent @event,
        CancellationToken cancellationToken);
}
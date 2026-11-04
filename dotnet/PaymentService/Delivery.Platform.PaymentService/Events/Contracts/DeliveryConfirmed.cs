namespace Delivery.Platform.PaymentService.Events.Contracts;

// ReSharper disable once ClassNeverInstantiated.Global
public sealed record DeliveryConfirmed(
    Guid EventId,
    Guid DeliveryId,
    decimal Price,
    string Currency,
    DateTimeOffset OccurredAt,
    Guid AggregateId,
    string Module,
    string EventType
) : IDeliveryEvent
{
}
using Delivery.Platform.Domain.Events;

namespace Delivery.Platform.PaymentService.Events.Contracts;

public sealed record PaymentDeclinedEvent(
    Guid EventId,
    Guid AggregateId,
    Guid PaymentId,
    Guid DeliveryId,
    DateTimeOffset OccurredAt,
    string Module = "payments",
    string EventType = PaymentEventNames.Declined
) : IEvent;
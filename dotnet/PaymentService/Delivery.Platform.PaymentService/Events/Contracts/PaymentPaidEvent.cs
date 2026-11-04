using Delivery.Platform.Domain.Events;

namespace Delivery.Platform.PaymentService.Events.Contracts;

public sealed record PaymentPaidEvent(
    Guid EventId,
    Guid PaymentId,
    Guid DeliveryId,
    DateTimeOffset OccurredAt,
    Guid AggregateId,
    string Module = "payments",
    string EventType = "payment.paid"
) : IEvent;
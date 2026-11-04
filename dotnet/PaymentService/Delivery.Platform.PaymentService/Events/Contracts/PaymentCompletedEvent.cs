using Delivery.Platform.Domain.Events;

namespace Delivery.Platform.PaymentService.Events.Contracts;

public sealed record PaymentCompletedEvent(
    Guid DeliveryId,
    Guid PaymentId,
    string ExternalTransactionId,
    decimal Amount,
    string Currency,
    Guid AggregateId,

    Guid EventId,
    DateTimeOffset OccurredAt,
    string Module = "payments",
    string EventType = PaymentEventNames.Completed) : IEvent
{

    public static PaymentCompletedEvent Create(
        Guid deliveryId,
        Guid paymentId,
        string externalTransactionId,
        decimal amount,
        string currency
    ) => new (deliveryId,
        paymentId,
        externalTransactionId,
        amount,
        currency,
        paymentId,
        EventId: Guid.NewGuid(),
        OccurredAt: DateTimeOffset.UtcNow);

}
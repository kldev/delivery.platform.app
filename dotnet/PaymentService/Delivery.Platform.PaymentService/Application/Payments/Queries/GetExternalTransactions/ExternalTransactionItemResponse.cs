namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;

public sealed record ExternalTransactionItemResponse(
    Guid Id,
    Guid PaymentId,
    Guid DeliveryId,
    string ExternalTransactionId,
    string Provider,
    decimal PaymentAmount,
    decimal Amount,
    string Currency,
    DateTimeOffset CreatedAt);
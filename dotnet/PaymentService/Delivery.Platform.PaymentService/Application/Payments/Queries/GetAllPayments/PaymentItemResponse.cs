namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;

public sealed record PaymentItemResponse(
    Guid Id,
    Guid DeliveryId,
    decimal Amount,
    string Currency,
    string Status,
    DateTimeOffset CreatedAt,
    DateTimeOffset? PaidAt);
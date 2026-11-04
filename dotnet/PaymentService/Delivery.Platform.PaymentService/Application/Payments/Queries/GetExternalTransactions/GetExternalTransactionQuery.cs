namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;

// ReSharper disable once ClassNeverInstantiated.Global
public sealed record GetExternalTransactionQuery(Guid? DeliveryId, 
    Guid? PaymentId, 
    string ExternalTransactionId);

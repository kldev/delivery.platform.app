using Delivery.Platform.Domain.Payments;

namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;

public static class ExternalTransactionExtensions
{
    public static IQueryable<PaymentExternalTransaction> BuildQuery(
        this IQueryable<PaymentExternalTransaction> queryable, GetExternalTransactionQuery query)
    {
        return queryable.WithExternalTransactions(query.ExternalTransactionId)
            .WithDeliveryId(query.DeliveryId)
            .WithPaymentId(query.PaymentId);
        
    }
    
    public static IQueryable<PaymentExternalTransaction> 
        WithPaymentId(this IQueryable<PaymentExternalTransaction> queryable, Guid? paymentId)
    {
        if (paymentId.HasValue)
        {
            queryable = queryable.Where(q=>q.PaymentId == paymentId.Value);
        }

        return queryable;
    }
    
    public static IQueryable<PaymentExternalTransaction> 
        WithExternalTransactions(this IQueryable<PaymentExternalTransaction> queryable, string externalTransactionId)
    {
        if (!string.IsNullOrWhiteSpace(externalTransactionId))
        {
            queryable = queryable.Where(q=>q.ExternalTransactionId.Contains(externalTransactionId));
        }

        return queryable;
    }

    
    
    public static IQueryable<PaymentExternalTransaction> WithDeliveryId(this IQueryable<PaymentExternalTransaction> queryable, Guid? deliveryId)
    {
        if (deliveryId.HasValue)
        {
            queryable = queryable.Where(q=>q.Payment.DeliveryId == deliveryId.Value);
        }

        return queryable;
    } 
}
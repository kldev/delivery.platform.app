using Delivery.Platform.Domain.Payments;

namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;

public  static class PaymentsQueryExtensions
{
    public static IQueryable<Payment> WithStatus(this IQueryable<Payment> queryable, PaymentStatus? status)
    {
        if (status.HasValue)
        {
            queryable = queryable.Where(q=>q.Status == status.Value);
        }

        return queryable;
    } 
    
    public static IQueryable<Payment> WithDeliveryId(this IQueryable<Payment> queryable, Guid? deliveryId)
    {
        if (deliveryId.HasValue)
        {
            queryable = queryable.Where(q=>q.DeliveryId == deliveryId.Value);
        }

        return queryable;
    } 
}
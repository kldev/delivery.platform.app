using Delivery.Platform.Domain.Payments;

namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;

public record GetPaymentsQuery(PaymentStatus? Status, Guid? deliveryId)
{
    
}
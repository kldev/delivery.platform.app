using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.PaymentService.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;

public sealed class GetPaymentsHandler(
    PaymentDbContext dbContext)
{
    public async Task<IReadOnlyList<PaymentItemResponse>> Handle(
        GetPaymentsQuery query,
        CancellationToken cancellationToken = default)
    {
        return await dbContext.Payments
            .AsNoTracking()
            .WithStatus(query.Status)
            .WithDeliveryId(query.deliveryId)
            .OrderByDescending(x => x.CreatedAt)
            .Select(x => new PaymentItemResponse(
                x.Id,
                x.DeliveryId,
                x.Amount,
                x.Currency,
                x.Status.ToString(),
                x.CreatedAt,
                x.PaidAt)).ToListAsync(cancellationToken);
    }
}
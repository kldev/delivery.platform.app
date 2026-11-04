using Delivery.Platform.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;

public sealed class GetExternalTransactionHandler(PaymentDbContext dbContext)
{
    public async Task<IReadOnlyList<ExternalTransactionItemResponse>> Handle(
        GetExternalTransactionQuery query,
        CancellationToken cancellationToken = default)
    {
        return await dbContext.External
            .AsNoTracking()
            .Include(p => p.Payment)
            .BuildQuery(query)
            .OrderByDescending(x => x.CreatedAt)
            .Select(x => new ExternalTransactionItemResponse(
                x.Id,
                x.PaymentId,
                x.Payment.DeliveryId,
                x.ExternalTransactionId,
                x.Provider,
                x.Payment.Amount,
                x.Amount,
                x.Payment.Currency,
                x.CreatedAt
            ))
            .ToListAsync(cancellationToken);
    }
}
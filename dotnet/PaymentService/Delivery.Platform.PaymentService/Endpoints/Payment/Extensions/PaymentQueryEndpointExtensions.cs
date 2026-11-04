using Delivery.Platform.Domain.Payments;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;
using Microsoft.AspNetCore.Mvc;

namespace Delivery.Platform.PaymentService.Endpoints.Payment.Extensions;

public static class PaymentQueryEndpointExtensions
{
    public static RouteGroupBuilder MapQueries(
        this RouteGroupBuilder group)
    {
        group.MapGet(
            "",
            async (
                    [FromServices] GetPaymentsHandler handler,
                    [FromQuery] PaymentStatus? status,
                    [FromQuery] Guid? deliveryId,
                    CancellationToken ct) =>
                TypedResults.Ok(
                    await handler.Handle(
                        new GetPaymentsQuery(status, deliveryId),
                        ct)));

        return group;
    }
}
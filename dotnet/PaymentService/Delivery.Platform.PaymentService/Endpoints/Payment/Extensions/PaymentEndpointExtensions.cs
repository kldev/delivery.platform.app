using Delivery.Platform.PaymentService.Application.Payments;
using Microsoft.AspNetCore.Mvc;

namespace Delivery.Platform.PaymentService.Endpoints.Payment.Extensions;

public static class PaymentEndpointExtensions
{
    public static RouteGroupBuilder MapPaymentActions(
        this RouteGroupBuilder group)
    {
        group.MapPut(
            "/{id}/{action}",
            async (
                [FromServices]PaymentActionService service,
                Guid id,
                PaymentAction action,
                CancellationToken ct) =>
            {
                await service.Execute(id, action, ct);

                return Results.NoContent();
            });

        return group;
    }
}

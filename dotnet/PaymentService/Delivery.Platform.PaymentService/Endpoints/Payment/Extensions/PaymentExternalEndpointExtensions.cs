using Delivery.Platform.PaymentService.Application.Payments.Commands.CreateExternal;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;
using Microsoft.AspNetCore.Mvc;

namespace Delivery.Platform.PaymentService.Endpoints.Payment.Extensions;

public static class PaymentExternalEndpointExtensions
{
    public static RouteGroupBuilder MapExternal(
        this RouteGroupBuilder group)
    {
        group.MapGet(
            "/external",
            async (
                    [FromServices] GetExternalTransactionHandler handler,
                    [FromQuery] Guid? paymentId,
                    [FromQuery] Guid? deliveryId,
                    [FromQuery] string? transactionId,
                    CancellationToken ct) =>
                TypedResults.Ok(
                    await handler.Handle(
                        new GetExternalTransactionQuery(
                            deliveryId,
                            paymentId,
                            transactionId ?? ""),
                        ct)));

        group.MapPost(
            "/external",
            async (
                    [FromServices] CreateExternalHandler handler,
                    CreateExternalCommand command,
                    CancellationToken ct) =>
                await handler.Handle(command, ct));

        return group;
    }
}
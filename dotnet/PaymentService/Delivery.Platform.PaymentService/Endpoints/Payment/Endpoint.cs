using Delivery.Platform.Domain.Payments;
using Delivery.Platform.PaymentService.Application.Payments;
using Delivery.Platform.PaymentService.Application.Payments.Commands.AcceptPayment;
using Delivery.Platform.PaymentService.Application.Payments.Commands.CreateExternal;
using Delivery.Platform.PaymentService.Application.Payments.Commands.DeclinePayment;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;
using Delivery.Platform.PaymentService.Endpoints.Payment.Extensions;
using Microsoft.AspNetCore.Mvc;

namespace Delivery.Platform.PaymentService.Endpoints.Payment;

public static class Endpoint
{
    public static void Map(WebApplication app)
    {
        app.MapGroup("/api/payments")
            .MapPaymentActions()
            .MapQueries()
            .MapExternal();
    }
}
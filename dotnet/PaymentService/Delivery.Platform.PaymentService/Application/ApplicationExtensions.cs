using Delivery.Platform.PaymentService.Application.Payments;
using Delivery.Platform.PaymentService.Application.Payments.Commands.AcceptPayment;
using Delivery.Platform.PaymentService.Application.Payments.Commands.CreateExternal;
using Delivery.Platform.PaymentService.Application.Payments.Commands.DeclinePayment;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetAllPayments;
using Delivery.Platform.PaymentService.Application.Payments.Queries.GetExternalTransactions;

namespace Delivery.Platform.PaymentService.Application;

public static class ApplicationExtensions
{
    public static IServiceCollection AddApplication(
        this IServiceCollection services)
    {
        services.AddScoped<AcceptPaymentHandler>();
        services.AddScoped<DeclinePaymentHandler>();
        services.AddScoped<GetPaymentsHandler>();
        services.AddScoped<CreateExternalHandler>();
        services.AddScoped<GetExternalTransactionHandler>();
        
        services.AddScoped<PaymentActionService>();

        return services;
    }
}
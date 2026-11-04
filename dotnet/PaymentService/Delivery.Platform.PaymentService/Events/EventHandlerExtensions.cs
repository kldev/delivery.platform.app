using Delivery.Platform.PaymentService.Application.Payments.ProcessDeliveryConfirmed;
using Delivery.Platform.PaymentService.Events.Contracts;
using Delivery.Platform.PaymentService.Events.Handlers;
using Delivery.Platform.PaymentService.Messaging;

namespace Delivery.Platform.PaymentService.Events;

public static class EventHandlerExtensions
{
    public static IServiceCollection AddEventHandlers(
        this IServiceCollection services)
    {
        services.AddKafkaListener<DeliveryConfirmed, DeliveryConfirmedHandler>("delivery.confirmed");

        services.AddScoped<ProcessDeliveryConfirmedHandler>();
        
        return services;
    }
}
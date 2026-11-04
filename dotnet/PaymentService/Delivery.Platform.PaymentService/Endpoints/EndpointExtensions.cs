namespace Delivery.Platform.PaymentService.Endpoints;

public static class EndpointExtensions
{
    public static void MapAppEndpoints(this WebApplication app)
    {
        Payment.Endpoint.Map(app);
    }
}
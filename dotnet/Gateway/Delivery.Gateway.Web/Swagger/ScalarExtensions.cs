using Scalar.AspNetCore;

namespace Delivery.Gateway.Web.Swagger;

public sealed record ScalarApiDocument(
    string Name,
    string Title);

public static class ScalarExtensions
{
    public static readonly ScalarApiDocument[] Services =
    [
        new("delivery", "Deliveries"),
        new( "payment", "Payments"),
        new("ledger", "Ledger"),
        new("settlement", "Settlement"),
        new ("reconciliation", "Reconciliation"),
        new ("notification", "Notification"),
        new ("reporting", "Reporting")
    ];

    private static void AddDeliveryPlatformDocuments(
        this ScalarOptions options)
    {
        foreach (var document in Services)
        {
            options.AddDocument(
                document.Name,
                document.Title,
                $"/api-docs/{document.Name}/swagger.json",
                true);
        }
    }

    public static void MapAppScalar(this WebApplication app)
    {
        app.MapScalarApiReference("/docs", options =>
        {
            options.Title = "Delivery Platform API";
            options.AddDeliveryPlatformDocuments();
        });
    }

}
using System.Text.Json;

namespace Delivery.Gateway.Web.Swagger;

public static class SwaggerEndpoints
{
    public static void MapSwaggerEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api-docs")
            .WithTags("API Docs")
            .ExcludeFromDescription();

        // Aggregated swagger for all services
        group.MapGet("/all/swagger.json", async (SwaggerAggregator aggregator) =>
        {
            var doc = await aggregator.GetAggregatedSwaggerAsync();
            return Results.Json(doc, new JsonSerializerOptions { WriteIndented = true });
        });

        // Individual service swagger with Gateway paths
        group.MapGet("/{service}/swagger.json", async (string service, SwaggerAggregator aggregator) =>
        {
            var doc = await aggregator.GetServiceSwaggerAsync(service);
            return doc != null
                ? Results.Json(doc, new JsonSerializerOptions { WriteIndented = true })
                : Results.NotFound($"Swagger not found for service: {service}");
        });
    }
}
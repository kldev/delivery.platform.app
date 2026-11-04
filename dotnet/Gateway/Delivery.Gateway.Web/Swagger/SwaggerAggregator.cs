using System.Text.Json.Nodes;

namespace Delivery.Gateway.Web.Swagger;

public class SwaggerAggregator(
    IHttpClientFactory httpClientFactory,
    IConfiguration configuration,
    ILogger<SwaggerAggregator> logger)
{
    private readonly HttpClient _httpClient = httpClientFactory.CreateClient("SwaggerClient");

    
    /// <summary>
    /// Gets swagger spec for a single service with paths prefixed for Gateway.
    /// </summary>
    public async Task<JsonObject?> GetServiceSwaggerAsync(string serviceName)
    {
        var service = ScalarExtensions.Services.FirstOrDefault(s =>
            s.Name.Equals(serviceName, StringComparison.OrdinalIgnoreCase));

        if (service == null)
        {
            logger.LogInformation("No service found for {Service}", serviceName);
            return null;
        }

        var baseUrl = GetClusterAddress($"{service.Name}-cluster");
        
        if (string.IsNullOrEmpty(baseUrl))
        {
            logger.LogInformation("No service URL found for {Service}.", serviceName);
            return null;
        }

        try
        {
            var swaggerUrl = serviceName == "payment"?  $"{baseUrl}/openapi/v1.json" : $"{baseUrl}/api-spec?format=json";
            if (serviceName == "notification")
                swaggerUrl = $"{baseUrl}/api-spec?format=json";
            
            var response = await _httpClient.GetStringAsync(swaggerUrl);
            var doc = JsonNode.Parse(response)?.AsObject();

            if (doc == null) return null;

            // Modify paths to include service prefix
            if (doc["paths"] is JsonObject paths)
            {
                var newPaths = new JsonObject();
                foreach (var (path, value) in paths)
                {
                    // /api/sbu/list -> /api/identity/sbu/list
                    var newPath = path.StartsWith("/api/")
                        ? $"/api/{service.Name}" + path[4..] // Remove "/api" and add prefix
                        : $"/{service.Name}" + path;

                    newPaths[newPath] = value?.DeepClone();
                }
                doc["paths"] = newPaths;
            }

            // Update info
            if (doc["info"] is JsonObject info)
            {
                info["title"] = $"Delivery {service.Name} API (via Gateway)";
            }
            
            if (doc["servers"] is JsonArray { Count: > 0 } servers &&
                servers[0] is JsonObject server)
            {
                server["url"] =configuration["Url"] ?? "";
            }

            return doc;
        }
        catch (Exception ex)
        {
            logger.LogWarning(ex, "Failed to fetch swagger from {Service} from url {Url}", serviceName, baseUrl);
            return null;
        }
    }

    /// <summary>
    /// Gets aggregated swagger spec combining all services.
    /// </summary>
    public async Task<JsonObject> GetAggregatedSwaggerAsync()
    {
        var aggregated = new JsonObject
        {
            ["openapi"] = "3.0.1",
            ["info"] = new JsonObject
            {
                ["title"] = "Delivery Gateway API",
                ["description"] = "Aggregated API documentation for all microservices",
                ["version"] = "v1"
            },
            ["paths"] = new JsonObject(),
            ["components"] = new JsonObject
            {
                ["schemas"] = new JsonObject()
            }
        };

        var allPaths = aggregated["paths"]!.AsObject();
        var allSchemas = aggregated["components"]!["schemas"]!.AsObject();

        foreach (var service in ScalarExtensions.Services)
        {
            var doc = await GetServiceSwaggerAsync(service.Name);
            if (doc == null) continue;

            // Merge paths
            if (doc["paths"] is JsonObject paths)
            {
                foreach (var (path, value) in paths)
                {
                    allPaths[path] = value?.DeepClone();
                }
            }

            // Merge schemas with service prefix to avoid conflicts
            if (doc["components"]?["schemas"] is not JsonObject schemas) continue;
            {
                foreach (var (schemaName, value) in schemas)
                {
                    var prefixedName = $"{service.Name}_{schemaName}";
                    allSchemas[prefixedName] = value?.DeepClone();
                }
            }
        }

        return aggregated;
    }

    private string? GetClusterAddress(string clusterKey)
    {
        return configuration[$"ReverseProxy:Clusters:{clusterKey}:Destinations:{clusterKey.Replace("-cluster", "")}:Address"];
    }
}
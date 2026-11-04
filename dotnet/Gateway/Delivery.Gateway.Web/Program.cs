using Delivery.Gateway.Web.Swagger;

var builder = WebApplication.CreateBuilder(args);
{
    builder.Services
        .AddReverseProxy()
        .LoadFromConfig(builder.Configuration.GetSection("ReverseProxy"));
    
    // HttpClient for swagger aggregation
    builder.Services.AddHttpClient("SwaggerClient", client =>
    {
        client.Timeout = TimeSpan.FromSeconds(10);
    });
    builder.Services.AddScoped<SwaggerAggregator>();
}
var app = builder.Build();
{

    app.MapAppScalar();
    app.MapReverseProxy();
    
    app.MapGet("/", () => "UP");
    //app.MapGet("/docs", () => Results.Redirect("/scalar"));
    
    app.MapSwaggerEndpoints();
    app.Run();
}

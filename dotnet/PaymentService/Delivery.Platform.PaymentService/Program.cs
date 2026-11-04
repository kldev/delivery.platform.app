using System.Text.Json.Nodes;
using System.Text.Json.Serialization;
using Delivery.Platform.Infrastructure;
using Delivery.Platform.PaymentService;
using Delivery.Platform.PaymentService.Application;
using Delivery.Platform.PaymentService.Endpoints;
using Delivery.Platform.PaymentService.Events;
using Delivery.Platform.PaymentService.Messaging;
using Delivery.Platform.PaymentService.Publisher;
using Scalar.AspNetCore;

var builder = WebApplication.CreateBuilder(args);
{
    builder.Services.ConfigureHttpJsonOptions(options =>
    {
        options.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });

    builder.Services.AddOpenApi();
    
    builder.Services.AddDatabase(builder.Configuration);
    builder.Services.AddKafka(
        builder.Configuration);

    builder.Services.AddEventHandlers();
    builder.Services.AddApplication();
    builder.Services.AddPublisher(builder.Configuration);
    builder.Services.AddExceptionHandler<GlobalExceptionHandler>();
    builder.Services.AddProblemDetails();
}

var app = builder.Build();
{
    await app.MigrateDatabaseAsync();
    app.MapAppEndpoints();
    app.MapOpenApi();
    app.MapScalarApiReference(options =>
    {
        options
            .WithTitle("Payment Service API")
            .WithTheme(ScalarTheme.Default);
    });
    
    app.UseExceptionHandler();

//    app.UseHttpsRedirection();

    app.MapGet("/healthz", () => "UP")
        .ExcludeFromDescription();

    app.MapGet("/docs", () => Results.Redirect("scalar/v1", permanent: true)).ExcludeFromDescription();

    app.Run();
}

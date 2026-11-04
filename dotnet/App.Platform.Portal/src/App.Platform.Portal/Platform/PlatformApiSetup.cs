using App.Platform.Portal.ApiClients.Delivery;
using App.Platform.Portal.ApiClients.Ledger;
using App.Platform.Portal.ApiClients.Notification;
using App.Platform.Portal.ApiClients.Payment;
using App.Platform.Portal.ApiClients.Reconciliation;
using App.Platform.Portal.ApiClients.Settlement;
using Microsoft.Extensions.Options;
using Microsoft.Kiota.Abstractions;
using Microsoft.Kiota.Abstractions.Authentication;
using Microsoft.Kiota.Http.HttpClientLibrary;

namespace App.Platform.Portal.Platform;

/// <summary>
/// The clients Kiota generated from the gateway's OpenAPI documents (App.Platform.Portal.ApiClients), one per
/// service, all sharing one request adapter over one HttpClient pointed at the gateway. The gateway is the only way to
/// the services, also for this portal.
/// </summary>
public static class PlatformApiSetup
{
    private const string HttpClientName = "gateway";

    public static IServiceCollection AddPlatformApiClients(
        this IServiceCollection services,
        IConfiguration configuration
    )
    {
        services
            .AddOptions<GatewayOptions>()
            .Bind(configuration.GetSection(GatewayOptions.Section))
            .ValidateDataAnnotations()
            .ValidateOnStart();

        // Kiota's default handlers (retry, redirect, parameters, user agent...) on the factory's HttpClient.
        var handlerTypes = KiotaClientFactory.GetDefaultHandlerActivatableTypes();
        foreach (var handler in handlerTypes)
            services.AddTransient(handler);

        var http = services.AddHttpClient(
            HttpClientName,
            (sp, client) =>
            {
                var gateway = sp.GetRequiredService<IOptions<GatewayOptions>>().Value;
                client.Timeout = gateway.Timeout;
            }
        );
        foreach (var handler in handlerTypes)
            http.AddHttpMessageHandler(sp => (DelegatingHandler)sp.GetRequiredService(handler));

        services.AddScoped<IRequestAdapter>(sp => new HttpClientRequestAdapter(
            new AnonymousAuthenticationProvider(),
            httpClient: sp.GetRequiredService<IHttpClientFactory>().CreateClient(HttpClientName)
        )
        {
            BaseUrl = sp.GetRequiredService<IOptions<GatewayOptions>>().Value.BaseUrl.TrimEnd('/'),
        });

        services.AddScoped(sp => new DeliveryApi(sp.GetRequiredService<IRequestAdapter>()));
        services.AddScoped(sp => new PaymentApi(sp.GetRequiredService<IRequestAdapter>()));
        services.AddScoped(sp => new SettlementApi(sp.GetRequiredService<IRequestAdapter>()));
        services.AddScoped(sp => new LedgerApi(sp.GetRequiredService<IRequestAdapter>()));
        services.AddScoped(sp => new ReconciliationApi(sp.GetRequiredService<IRequestAdapter>()));
        services.AddScoped(sp => new NotificationApi(sp.GetRequiredService<IRequestAdapter>()));
        return services;
    }
}

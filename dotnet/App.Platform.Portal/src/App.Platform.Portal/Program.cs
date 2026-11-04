using App.Platform.Portal.Components;
using App.Platform.Portal.Platform;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents().AddInteractiveServerComponents();
builder.Services.AddPlatformApiClients(builder.Configuration);
builder
    .Services.AddOptions<PortalOptions>()
    .Bind(builder.Configuration.GetSection(PortalOptions.Section))
    .ValidateDataAnnotations()
    .Validate(
        o => TimeZoneInfo.TryFindSystemTimeZoneById(o.TimeZone, out _),
        "Portal:TimeZone is not a known time zone."
    )
    .ValidateOnStart();
builder.Services.AddSingleton(TimeProvider.System);
builder.Services.AddSingleton<PortalClock>();
builder.Services.AddScoped<Toasts>();
builder.Services.AddHealthChecks();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
}

// No HTTPS redirection: the container listens on HTTP only, TLS ends at the proxy in front of it.
app.UseStatusCodePagesWithReExecute("/not-found", createScopeForStatusCodePages: true);
app.UseAntiforgery();

// Liveness only: the portal is up even when a service is not - its pages say which one does not answer.
app.MapHealthChecks("/health");
app.MapStaticAssets();
app.MapRazorComponents<PortalApp>().AddInteractiveServerRenderMode();

app.Run();

/// <summary>Entry point, visible to the tests' WebApplicationFactory.</summary>
public partial class Program;

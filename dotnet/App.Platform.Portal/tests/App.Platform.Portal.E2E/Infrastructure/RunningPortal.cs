using System.Net.Http.Json;

namespace App.Platform.Portal.E2E.Infrastructure;

/// <summary>
/// The running stack: the portal (<c>PORTAL_URL</c>, default http://localhost:5080) and the gateway behind it
/// (<c>GATEWAY_URL</c>, default http://localhost:5000). One Chromium for the whole run. The tests create their own
/// drivers and deliveries with unique addresses, so they work on a stack that already has data.
/// </summary>
public sealed class RunningPortal : IAsyncLifetime
{
    public static string BaseUrl { get; } =
        (Environment.GetEnvironmentVariable("PORTAL_URL") ?? "http://localhost:5080").TrimEnd('/');

    public static string GatewayUrl { get; } =
        (Environment.GetEnvironmentVariable("GATEWAY_URL") ?? "http://localhost:5000").TrimEnd('/');

    private IPlaywright? _playwright;
    private IBrowser? _browser;

    public HttpClient Gateway { get; } = new() { BaseAddress = new Uri(GatewayUrl + "/") };

    public async ValueTask InitializeAsync()
    {
        await RequireAsync($"{BaseUrl}/health", "the portal");
        await RequireAsync(
            $"{GatewayUrl}/api/delivery/drivers",
            "the gateway and the delivery service"
        );

        // Installs Chromium on the first run (no-op when it is there).
        var exit = Microsoft.Playwright.Program.Main(["install", "chromium"]);
        if (exit != 0)
            throw new InvalidOperationException($"playwright install chromium exited with {exit}");

        _playwright = await Playwright.CreateAsync();
        _browser = await _playwright.Chromium.LaunchAsync(new() { Headless = true });
    }

    /// <summary>A fresh browser context (own circuit) on a desktop or a phone viewport.</summary>
    public async Task<IPage> OpenAsync(bool phone = false)
    {
        var context = await _browser!.NewContextAsync(
            new()
            {
                BaseURL = BaseUrl + "/",
                ViewportSize = phone
                    ? new() { Width = 390, Height = 844 }
                    : new() { Width = 1440, Height = 900 },
                DeviceScaleFactor = phone ? 2 : 1,
                IsMobile = phone,
                HasTouch = phone,
                Locale = "pl-PL",
                TimezoneId = "Europe/Warsaw",
            }
        );
        context.SetDefaultTimeout(15_000);
        return await context.NewPageAsync();
    }

    /// <summary>Opens a page and waits until the Blazor circuit is up (the page's own h1 is rendered).</summary>
    public static async Task GotoAsync(IPage page, string path)
    {
        await page.GotoAsync(path);
        await Expect(page.Locator("main h1").First).ToBeVisibleAsync();
    }

    /// <summary>A street address with a flat number, so every run's orders look a little different.</summary>
    public static string Flat(string street) => $"{street}/{Random.Shared.Next(2, 90)}";

    /// <summary>A driver of its own, so the automatic assignment always has someone free.</summary>
    public async Task<(Guid Id, string Name)> CreateDriverAsync(string first, string last)
    {
        var response = await Gateway.PostAsJsonAsync(
            "api/delivery/drivers",
            new
            {
                firstName = first,
                lastName = last,
                phoneNumber = "+48600" + Random.Shared.Next(100000, 999999),
                email = $"{first}.{last}.{Guid.NewGuid().ToString("N")[..6]}@example.com".ToLowerInvariant(),
            }
        );
        response.EnsureSuccessStatusCode();
        var body = await response.Content.ReadFromJsonAsync<IdOnly>();
        return (body!.Id, $"{first} {last}");
    }

    private sealed record IdOnly(Guid Id);

    private static async Task RequireAsync(string url, string what)
    {
        using var http = new HttpClient { Timeout = TimeSpan.FromSeconds(5) };
        try
        {
            (await http.GetAsync(url)).EnsureSuccessStatusCode();
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException(
                $"E2E needs {what} at {url}. Start the stack from the repo root: docker compose up -d --build. ({ex.Message})",
                ex
            );
        }
    }

    public async ValueTask DisposeAsync()
    {
        if (_browser is not null)
            await _browser.DisposeAsync();
        _playwright?.Dispose();
        Gateway.Dispose();
    }
}

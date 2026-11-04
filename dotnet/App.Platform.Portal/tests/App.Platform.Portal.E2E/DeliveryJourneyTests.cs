using App.Platform.Portal.E2E.Infrastructure;

namespace App.Platform.Portal.E2E;

/// <summary>
/// One delivery's whole route, clicked through the portal: the order, confirmation, the payment (created by the
/// payment service from delivery.confirmed), acceptance, the driver assigned by the delivery service from payment.paid,
/// pickup, transit, delivery, the driver's settlement and ledger, the provider's transaction and the reconciliation.
/// Every step asserts what the screen should show; the screenshots are the pictures of docs/Portal.md.
/// </summary>
[Collection(PortalTests.Name)]
public class DeliveryJourneyTests(RunningPortal portal)
{
    private static readonly TimeSpan Kafka = TimeSpan.FromSeconds(40);
    private readonly Screens _screens = new();

    [Fact]
    public async Task A_delivery_goes_the_whole_route_and_is_reconciled()
    {
        await portal.CreateDriverAsync("Tomasz", "Wiśniewski");
        var page = await portal.OpenAsync();

        // The form checks the fields before it asks the service.
        await RunningPortal.GotoAsync(page, "deliveries/new");
        await page.GetByRole(AriaRole.Button, new() { Name = "Utwórz zlecenie" }).ClickAsync();
        await Expect(page.GetByText("Podaj adres odbioru.")).ToBeVisibleAsync();
        await Expect(page.GetByText("Dystans to co najmniej 0,1 km.")).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "03-new-delivery-validation");

        var pickup = RunningPortal.Flat("Kraków, ul. Floriańska 3");
        await page.Locator("#pickup").FillAsync(pickup);
        await page.Locator("#dropoff").FillAsync("Kraków, ul. Długa 10");
        await page.Locator("#distance").FillAsync("12.5");
        await page.GetByRole(AriaRole.Button, new() { Name = "Utwórz zlecenie" }).ClickAsync();

        // Created: the price from the delivery service, the next step is confirmation.
        await Expect(page)
            .ToHaveURLAsync(new System.Text.RegularExpressions.Regex("/deliveries/[0-9a-f-]{36}$"));
        await Expect(page.Locator(".toast")).ToContainTextAsync("Zlecenie utworzone");
        await Expect(page.GetByText(pickup)).ToBeVisibleAsync();
        var confirm = page.GetByRole(AriaRole.Button, new() { Name = "Potwierdź zlecenie" });
        await Expect(confirm).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "04-delivery-created", keepToasts: true);

        // Confirmed: the payment service creates the payment from delivery.confirmed; the trail waits for it.
        await confirm.ClickAsync();
        var accept = page.GetByRole(AriaRole.Button, new() { Name = "Akceptuj" });
        await Expect(accept).ToBeVisibleAsync(new() { Timeout = (float)Kafka.TotalMilliseconds });
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Czeka na płatność" }))
            .ToBeVisibleAsync();
        await _screens.ShotAsync(page, "05-payment-pending");

        // Accepted: payment.paid -> the delivery service assigns a free driver by itself.
        await accept.ClickAsync();
        var pickupButton = page.GetByRole(AriaRole.Button, new() { Name = "Oznacz odbiór paczki" });
        await Expect(pickupButton)
            .ToBeVisibleAsync(new() { Timeout = (float)Kafka.TotalMilliseconds });
        await Expect(Fact(page, "Kierowca")).Not.ToHaveTextAsync("-");
        await _screens.ShotAsync(page, "06-driver-assigned");

        await pickupButton.ClickAsync();
        var start = page.GetByRole(AriaRole.Button, new() { Name = "Wyślij w drogę" });
        await Expect(start).ToBeVisibleAsync();
        await start.ClickAsync();
        var complete = page.GetByRole(AriaRole.Button, new() { Name = "Potwierdź doręczenie" });
        await Expect(complete).ToBeVisibleAsync();
        await Expect(Fact(page, "Etap")).ToContainTextAsync("W drodze");
        await _screens.ShotAsync(page, "07-in-transit");

        // Delivered: delivery.completed -> settlement -> ledger.
        await complete.ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Dostawa zakończona" }))
            .ToBeVisibleAsync();
        await Expect(page.GetByRole(AriaRole.Link, new() { Name = "Zapisy w księdze" }))
            .ToBeVisibleAsync(new() { Timeout = (float)Kafka.TotalMilliseconds });
        var simulate = page.GetByRole(
            AriaRole.Button,
            new() { Name = "Symuluj wpłatę od operatora" }
        );
        await Expect(simulate).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "08-delivered-settled");

        // The provider's transaction: payment.completed -> reconciliation compares it with the settlement.
        await simulate.ClickAsync();
        await Expect(page.GetByRole(AriaRole.Link, new() { Name = "Sprawa uzgodnienia" }))
            .ToBeVisibleAsync(new() { Timeout = (float)Kafka.TotalMilliseconds });
        await Expect(TrailStep(page, "Uzgodnienie"))
            .Not.ToContainTextAsync(
                "porównuje kwoty",
                new() { Timeout = (float)Kafka.TotalMilliseconds }
            );
        await _screens.ShotAsync(page, "09-reconciled");

        // What the other services wrote, one link away.
        await page.GetByRole(AriaRole.Link, new() { Name = "Szczegóły rozliczenia" }).ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Rozliczenia kierowców" }))
            .ToBeVisibleAsync();
        await Expect(page.Locator("table.data tbody tr")).ToHaveCountAsync(1);
        await _screens.ShotAsync(page, "10-settlement-of-the-delivery");

        await page.GoBackAsync();
        await page.GetByRole(AriaRole.Link, new() { Name = "Zapisy w księdze" }).ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Księga", Exact = true }))
            .ToBeVisibleAsync();
        await Expect(page.GetByText("Transakcje rozliczenia")).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "11-ledger-transaction");

        await page.GoBackAsync();
        await page.GetByRole(AriaRole.Link, new() { Name = "Sprawa uzgodnienia" }).ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Uzgodnienia" }))
            .ToBeVisibleAsync();
        await Expect(page.Locator("table.data tbody tr").First).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "12-reconciliation-of-the-delivery");
    }

    internal static ILocator Fact(IPage page, string label) =>
        page.Locator(".fact")
            .Filter(new() { Has = page.Locator(".fact-label", new() { HasTextString = label }) })
            .Locator(".fact-value");

    internal static ILocator TrailStep(IPage page, string title) =>
        page.Locator(".trail-step")
            .Filter(new() { Has = page.Locator(".trail-title", new() { HasTextString = title }) });
}

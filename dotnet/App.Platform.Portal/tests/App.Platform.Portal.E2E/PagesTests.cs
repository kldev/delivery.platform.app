using App.Platform.Portal.E2E.Infrastructure;

namespace App.Platform.Portal.E2E;

/// <summary>Every page of the portal answers with the services' data; the drivers' form; a wrong address; the phone.</summary>
[Collection(PortalTests.Name)]
public class PagesTests(RunningPortal portal)
{
    private readonly Screens _screens = new();

    [Fact]
    public async Task The_overview_and_the_lists_show_the_services_data()
    {
        var page = await portal.OpenAsync();

        await RunningPortal.GotoAsync(page, "");
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Sieć dostaw" }))
            .ToBeVisibleAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Ostatnie zlecenia" }))
            .ToBeVisibleAsync();
        await _screens.ShotAsync(page, "01-overview");

        await RunningPortal.GotoAsync(page, "deliveries");
        await Expect(page.Locator("table.data tbody tr").First).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "02-deliveries");

        // A stage filter lives in the URL.
        await page.GetByRole(AriaRole.Group, new() { Name = "Etap" })
            .GetByRole(AriaRole.Button, new() { Name = "Doręczona" })
            .ClickAsync();
        await Expect(page)
            .ToHaveURLAsync(new System.Text.RegularExpressions.Regex("status=DELIVERED"));
        await Expect(page.Locator("table.data tbody tr .pill").First)
            .ToContainTextAsync("Doręczona");
        await _screens.ShotAsync(page, "16-deliveries-delivered");

        // A search needs the full id.
        await RunningPortal.GotoAsync(page, "deliveries?q=abc");
        await Expect(page.GetByText("Wpisz pełny identyfikator dostawy")).ToBeVisibleAsync();

        foreach (
            var (path, heading, shot) in new[]
            {
                ("payments", "Płatności", "18-payments"),
                ("settlements", "Rozliczenia kierowców", "19-settlements"),
                ("ledger", "Księga", "20-ledger"),
                ("reconciliations", "Uzgodnienia", "21-reconciliations"),
                ("notifications", "Powiadomienia", "22-notifications"),
            }
        )
        {
            await RunningPortal.GotoAsync(page, path);
            await Expect(page.GetByRole(AriaRole.Heading, new() { Name = heading, Exact = true }))
                .ToBeVisibleAsync();
            await Expect(page.Locator(".notice[role=alert]")).ToHaveCountAsync(0);
            await _screens.ShotAsync(page, shot);
        }
    }

    [Fact]
    public async Task A_driver_is_registered_from_the_form()
    {
        var page = await portal.OpenAsync();
        await RunningPortal.GotoAsync(page, "drivers");
        await page.GetByRole(AriaRole.Button, new() { Name = "Dodaj kierowcę" }).First.ClickAsync();

        var email = $"k.zielinska.{Guid.NewGuid().ToString("N")[..6]}@example.com";
        await page.Locator("#first").FillAsync("Katarzyna");
        await page.Locator("#last").FillAsync("Zielińska");
        await page.Locator("#phone").FillAsync("+48601" + Random.Shared.Next(100000, 999999));
        await page.Locator("#email").FillAsync(email);
        await _screens.ShotAsync(page, "17-new-driver-form");

        await page.GetByRole(AriaRole.Button, new() { Name = "Zapisz kierowcę" }).ClickAsync();
        await Expect(page.Locator(".toast")).ToContainTextAsync("Kierowca dodany");
        await Expect(page.GetByRole(AriaRole.Row).Filter(new() { HasTextString = email }))
            .ToContainTextAsync("Katarzyna Zielińska");
    }

    [Fact]
    public async Task A_wrong_address_shows_the_not_found_page()
    {
        var page = await portal.OpenAsync();
        await page.GotoAsync("no-such-page");
        await Expect(page.GetByText("Tu nic nie ma")).ToBeVisibleAsync();

        await page.GotoAsync($"deliveries/{Guid.NewGuid()}");
        await Expect(page.GetByText("Nie ma takiej dostawy")).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "23-delivery-not-found");
    }

    [Fact]
    public async Task On_a_phone_the_rail_folds_into_a_menu()
    {
        var page = await portal.OpenAsync(phone: true);
        await RunningPortal.GotoAsync(page, "deliveries");
        await Expect(page.Locator(".topbar")).ToBeVisibleAsync();
        await Expect(page.Locator("table.data tbody tr").First).ToBeVisibleAsync();
        // The page itself never scrolls sideways: the table scrolls inside its own box.
        (
            await page.EvaluateAsync<int>(
                "document.documentElement.scrollWidth - document.documentElement.clientWidth"
            )
        ).ShouldBeLessThanOrEqualTo(0);
        await _screens.ShotAsync(page, "24-phone-deliveries", fullPage: false);

        await page.GetByRole(AriaRole.Button, new() { Name = "Menu" }).ClickAsync();
        await Expect(page.Locator(".shell.nav-open")).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "25-phone-menu", fullPage: false);

        await page.Locator("nav.nav")
            .GetByRole(AriaRole.Link, new() { Name = "Płatności" })
            .ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Płatności" }))
            .ToBeVisibleAsync();
        await Expect(page.Locator(".shell.nav-open")).ToHaveCountAsync(0);
    }
}

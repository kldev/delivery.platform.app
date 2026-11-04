using App.Platform.Portal.E2E.Infrastructure;

namespace App.Platform.Portal.E2E;

/// <summary>The two ways a delivery stops: a declined payment, and a dispatcher's cancel on the way.</summary>
[Collection(PortalTests.Name)]
public class StoppedDeliveryTests(RunningPortal portal)
{
    private static readonly float Kafka = 40_000;
    private readonly Screens _screens = new();

    [Fact]
    public async Task A_declined_payment_cancels_the_delivery()
    {
        var page = await portal.OpenAsync();
        await CreateAsync(
            page,
            RunningPortal.Flat("Poznań, ul. Półwiejska 2"),
            "Poznań, ul. Garbary 48",
            "4"
        );

        await page.GetByRole(AriaRole.Button, new() { Name = "Potwierdź zlecenie" }).ClickAsync();
        var decline = page.GetByRole(AriaRole.Button, new() { Name = "Odrzuć" });
        await Expect(decline).ToBeVisibleAsync(new() { Timeout = Kafka });
        await decline.ClickAsync();

        // payment.declined -> the delivery service cancels it at "Potwierdzona".
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Dostawa anulowana" }))
            .ToBeVisibleAsync(new() { Timeout = Kafka });
        await Expect(page.GetByText("Zatrzymana na etapie: Potwierdzona")).ToBeVisibleAsync();
        await _screens.ShotAsync(page, "13-payment-declined");
    }

    [Fact]
    public async Task A_dispatcher_cancels_a_delivery_after_a_question()
    {
        var page = await portal.OpenAsync();
        await CreateAsync(
            page,
            RunningPortal.Flat("Gdańsk, ul. Długa 1"),
            "Sopot, ul. Monte Cassino 5",
            "15"
        );

        await page.GetByRole(AriaRole.Button, new() { Name = "Anuluj dostawę" }).ClickAsync();
        var dialog = page.GetByRole(AriaRole.Alertdialog);
        await Expect(dialog).ToContainTextAsync("Anulować tę dostawę?");
        await _screens.ShotAsync(page, "14-cancel-dialog", fullPage: false);

        await dialog.GetByRole(AriaRole.Button, new() { Name = "Tak, anuluj" }).ClickAsync();
        await Expect(page.GetByRole(AriaRole.Heading, new() { Name = "Dostawa anulowana" }))
            .ToBeVisibleAsync();
        await Expect(page.GetByText("Zatrzymana na etapie: Nowa")).ToBeVisibleAsync();
        await Expect(page.GetByRole(AriaRole.Button, new() { Name = "Anuluj dostawę" }))
            .ToHaveCountAsync(0);
        await _screens.ShotAsync(page, "15-cancelled");
    }

    private static async Task CreateAsync(IPage page, string pickup, string dropoff, string km)
    {
        await RunningPortal.GotoAsync(page, "deliveries/new");
        await page.Locator("#pickup").FillAsync(pickup);
        await page.Locator("#dropoff").FillAsync(dropoff);
        await page.Locator("#distance").FillAsync(km);
        await page.GetByRole(AriaRole.Button, new() { Name = "Utwórz zlecenie" }).ClickAsync();
        await Expect(page.GetByRole(AriaRole.Button, new() { Name = "Potwierdź zlecenie" }))
            .ToBeVisibleAsync();
    }
}

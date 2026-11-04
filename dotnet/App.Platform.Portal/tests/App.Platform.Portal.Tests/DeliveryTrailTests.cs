using App.Platform.Portal.Platform;

namespace App.Platform.Portal.Tests;

public class DeliveryTrailTests
{
    private static TrailFacts Facts(
        DeliveryStage stage,
        string? payment = null,
        bool external = false,
        bool driver = false,
        bool settlement = false,
        int ledger = 0,
        string? reconciliation = null,
        DeliveryStage? cancelledAt = null
    ) =>
        new(
            stage,
            cancelledAt,
            payment,
            "10,00 PLN",
            external,
            driver ? "Anna Kowalska" : null,
            driver,
            settlement ? "7,00 PLN" : null,
            settlement,
            ledger,
            reconciliation,
            "3,00 PLN"
        );

    private static TrailState StateOf(TrailFacts facts, TrailStepKind kind) =>
        DeliveryTrail.Build(facts).Single(s => s.Kind == kind).State;

    [Fact]
    public void A_new_delivery_waits_for_nothing()
    {
        var steps = DeliveryTrail.Build(Facts(DeliveryStage.Created));

        steps.ShouldAllBe(s => s.State == TrailState.Later);
        DeliveryTrail.AwaitsServices(steps).ShouldBeFalse();
    }

    [Fact]
    public void After_confirming_the_page_waits_for_the_payment_service()
    {
        var steps = DeliveryTrail.Build(Facts(DeliveryStage.Confirmed));

        StateOf(Facts(DeliveryStage.Confirmed), TrailStepKind.Payment).ShouldBe(TrailState.Waiting);
        DeliveryTrail.AwaitsServices(steps).ShouldBeTrue();
    }

    [Fact]
    public void A_pending_payment_waits_for_a_person_not_for_a_service()
    {
        var facts = Facts(DeliveryStage.Confirmed, payment: "Pending");

        StateOf(facts, TrailStepKind.Payment).ShouldBe(TrailState.Action);
        DeliveryTrail.AwaitsServices(DeliveryTrail.Build(facts)).ShouldBeFalse();
    }

    [Fact]
    public void An_accepted_payment_makes_the_page_wait_for_the_driver()
    {
        StateOf(Facts(DeliveryStage.Confirmed, payment: "Paid"), TrailStepKind.Driver)
            .ShouldBe(TrailState.Waiting);
        StateOf(Facts(DeliveryStage.Assigned, payment: "Paid", driver: true), TrailStepKind.Driver)
            .ShouldBe(TrailState.Done);
    }

    [Fact]
    public void An_accepted_payment_alone_opens_no_reconciliation()
    {
        // payment.completed comes only with the provider's transaction (POST /payments/external).
        StateOf(
                Facts(DeliveryStage.Assigned, payment: "Paid", driver: true),
                TrailStepKind.Reconciliation
            )
            .ShouldBe(TrailState.Later);
        StateOf(
                Facts(DeliveryStage.Assigned, payment: "Paid", external: true, driver: true),
                TrailStepKind.Reconciliation
            )
            .ShouldBe(TrailState.Waiting);
    }

    [Fact]
    public void A_delivered_delivery_waits_for_its_settlement_then_for_the_ledger()
    {
        StateOf(
                Facts(DeliveryStage.Delivered, payment: "Paid", driver: true),
                TrailStepKind.Settlement
            )
            .ShouldBe(TrailState.Waiting);

        var settled = Facts(
            DeliveryStage.Delivered,
            payment: "Paid",
            driver: true,
            settlement: true
        );
        StateOf(settled, TrailStepKind.Settlement).ShouldBe(TrailState.Done);
        StateOf(settled, TrailStepKind.Ledger).ShouldBe(TrailState.Waiting);
        StateOf(settled with { LedgerTransactions = 1 }, TrailStepKind.Ledger)
            .ShouldBe(TrailState.Done);
    }

    [Fact]
    public void A_reconciliation_with_one_side_waits_for_the_other_without_polling()
    {
        var facts = Facts(
            DeliveryStage.Delivered,
            payment: "Paid",
            driver: true,
            settlement: true,
            ledger: 1,
            reconciliation: "PENDING"
        );

        StateOf(facts, TrailStepKind.Reconciliation).ShouldBe(TrailState.Partial);
        DeliveryTrail.AwaitsServices(DeliveryTrail.Build(facts)).ShouldBeFalse();
    }

    [Theory]
    [InlineData("RECONCILED", TrailState.Done)]
    [InlineData("DISCREPANCY", TrailState.Failed)]
    [InlineData("MISSING_PAYMENT", TrailState.Failed)]
    public void A_closed_reconciliation_shows_its_outcome(string status, TrailState expected) =>
        StateOf(
                Facts(
                    DeliveryStage.Delivered,
                    payment: "Paid",
                    external: true,
                    driver: true,
                    settlement: true,
                    ledger: 1,
                    reconciliation: status
                ),
                TrailStepKind.Reconciliation
            )
            .ShouldBe(expected);

    [Fact]
    public void A_delivery_cancelled_before_confirmation_never_gets_a_payment()
    {
        var steps = DeliveryTrail.Build(
            Facts(DeliveryStage.Cancelled, cancelledAt: DeliveryStage.Created)
        );

        steps.ShouldAllBe(s => s.State == TrailState.Later);
        DeliveryTrail.AwaitsServices(steps).ShouldBeFalse();
    }

    [Fact]
    public void A_declined_payment_is_shown_as_failed()
    {
        StateOf(
                Facts(
                    DeliveryStage.Cancelled,
                    payment: "Failed",
                    cancelledAt: DeliveryStage.Confirmed
                ),
                TrailStepKind.Payment
            )
            .ShouldBe(TrailState.Failed);
    }
}

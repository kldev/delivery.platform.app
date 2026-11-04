namespace App.Platform.Portal.Platform;

/// <summary>How far a downstream service got with a delivery.</summary>
public enum TrailState
{
    /// <summary>Not yet its turn (or not applicable).</summary>
    Later,

    /// <summary>Its turn: the service should answer soon - the page keeps asking.</summary>
    Waiting,

    /// <summary>Waits for a person (a pending payment).</summary>
    Action,

    /// <summary>Started, waits for the other half (a reconciliation with one side only).</summary>
    Partial,

    Done,
    Failed,
}

public enum TrailStepKind
{
    Payment,
    Driver,
    Settlement,
    Ledger,
    Reconciliation,
}

public sealed record TrailStep(
    TrailStepKind Kind,
    string Title,
    string Service,
    TrailState State,
    string Text,
    string Icon
);

/// <summary>What the services know about one delivery, read on the delivery page.</summary>
public sealed record TrailFacts(
    DeliveryStage Stage,
    DeliveryStage? CancelledAt,
    string? PaymentStatus,
    string? PaymentAmount,
    bool HasExternalTransaction,
    string? DriverName,
    bool HasDriver,
    string? SettlementText,
    bool HasSettlement,
    int LedgerTransactions,
    string? ReconciliationStatus,
    string? ReconciliationDifference
);

/// <summary>
/// The trail a delivery leaves in the other services, step by step: the payment (payment service), the driver
/// (delivery service, after payment.paid), the driver's settlement (settlement service, after delivery.completed), its
/// ledger transaction (ledger service) and the reconciliation of the money (reconciliation service). A reconciliation
/// case opens on settlement.created or on payment.completed - the latter only when the payment provider reports its
/// transaction (POST /payments/external), not when the payment is accepted.
/// </summary>
public static class DeliveryTrail
{
    public static IReadOnlyList<TrailStep> Build(TrailFacts facts)
    {
        var cancelled = facts.Stage == DeliveryStage.Cancelled;
        var paid = facts.PaymentStatus == "Paid";
        var notApplicable = "Nie dotyczy - dostawa anulowana.";

        return [Payment(), Driver(), Settlement(), Ledger(), Reconciliation()];

        TrailStep Payment()
        {
            const string title = "Płatność";
            const string service = "payment-service";
            return facts.PaymentStatus switch
            {
                "Paid" when facts.HasExternalTransaction => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Done,
                    $"Opłacona: {facts.PaymentAmount}. Operator potwierdził wpłatę.",
                    "credit-card"
                ),
                "Paid" => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Done,
                    $"Opłacona: {facts.PaymentAmount}. Operator płatności jeszcze nie potwierdził wpłaty.",
                    "credit-card"
                ),
                "Failed" => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Failed,
                    $"Odrzucona: {facts.PaymentAmount}.",
                    "credit-card"
                ),
                "Pending" when cancelled => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Later,
                    $"Nieopłacona ({facts.PaymentAmount}), dostawa anulowana.",
                    "credit-card"
                ),
                "Pending" => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Action,
                    $"Czeka na akceptację: {facts.PaymentAmount}.",
                    "credit-card"
                ),
                _ when facts.Stage == DeliveryStage.Created => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Later,
                    "Powstanie po potwierdzeniu zlecenia.",
                    "credit-card"
                ),
                _ when cancelled && facts.CancelledAt is null or DeliveryStage.Created => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Later,
                    "Nie dotyczy - anulowana przed potwierdzeniem.",
                    "credit-card"
                ),
                _ => new(
                    TrailStepKind.Payment,
                    title,
                    service,
                    TrailState.Waiting,
                    "Serwis płatności zakłada płatność.",
                    "credit-card"
                ),
            };
        }

        TrailStep Driver()
        {
            const string title = "Kierowca";
            const string service = "delivery-service";
            if (facts.HasDriver)
                return new(
                    TrailStepKind.Driver,
                    title,
                    service,
                    TrailState.Done,
                    facts.DriverName ?? "Przydzielony.",
                    "user-check"
                );
            if (cancelled)
                return new(
                    TrailStepKind.Driver,
                    title,
                    service,
                    TrailState.Later,
                    notApplicable,
                    "user-check"
                );
            if (paid && facts.Stage == DeliveryStage.Confirmed)
                return new(
                    TrailStepKind.Driver,
                    title,
                    service,
                    TrailState.Waiting,
                    "Serwis dostaw szuka wolnego kierowcy.",
                    "user-check"
                );
            return new(
                TrailStepKind.Driver,
                title,
                service,
                TrailState.Later,
                "Zostanie przydzielony po opłaceniu.",
                "user-check"
            );
        }

        TrailStep Settlement()
        {
            const string title = "Rozliczenie kierowcy";
            const string service = "settlement-service";
            if (facts.HasSettlement)
                return new(
                    TrailStepKind.Settlement,
                    title,
                    service,
                    TrailState.Done,
                    facts.SettlementText ?? "Rozliczona.",
                    "hand-coins"
                );
            if (facts.Stage == DeliveryStage.Delivered)
                return new(
                    TrailStepKind.Settlement,
                    title,
                    service,
                    TrailState.Waiting,
                    "Serwis rozliczeń liczy wynagrodzenie.",
                    "hand-coins"
                );
            if (cancelled)
                return new(
                    TrailStepKind.Settlement,
                    title,
                    service,
                    TrailState.Later,
                    notApplicable,
                    "hand-coins"
                );
            return new(
                TrailStepKind.Settlement,
                title,
                service,
                TrailState.Later,
                "Po doręczeniu.",
                "hand-coins"
            );
        }

        TrailStep Ledger()
        {
            const string title = "Księga";
            const string service = "ledger-service";
            if (facts.LedgerTransactions > 0)
                return new(
                    TrailStepKind.Ledger,
                    title,
                    service,
                    TrailState.Done,
                    facts.LedgerTransactions == 1
                        ? "Rozliczenie zaksięgowane."
                        : $"Zaksięgowane transakcje: {facts.LedgerTransactions}.",
                    "book-open"
                );
            if (facts.HasSettlement)
                return new(
                    TrailStepKind.Ledger,
                    title,
                    service,
                    TrailState.Waiting,
                    "Księga zapisuje rozliczenie.",
                    "book-open"
                );
            if (cancelled)
                return new(
                    TrailStepKind.Ledger,
                    title,
                    service,
                    TrailState.Later,
                    notApplicable,
                    "book-open"
                );
            return new(
                TrailStepKind.Ledger,
                title,
                service,
                TrailState.Later,
                "Po rozliczeniu kierowcy.",
                "book-open"
            );
        }

        TrailStep Reconciliation()
        {
            const string title = "Uzgodnienie";
            const string service = "reconciliation-service";
            return facts.ReconciliationStatus switch
            {
                "RECONCILED" => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Done,
                    "Kwoty płatności i rozliczenia się zgadzają.",
                    "scale"
                ),
                "DISCREPANCY" => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Failed,
                    $"Rozbieżność: {facts.ReconciliationDifference}.",
                    "scale"
                ),
                "MISSING_PAYMENT" => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Failed,
                    "Brak płatności do rozliczenia.",
                    "scale"
                ),
                "MISSING_SETTLEMENT" => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Failed,
                    "Brak rozliczenia do płatności.",
                    "scale"
                ),
                "PENDING" when facts.HasSettlement && facts.HasExternalTransaction => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Waiting,
                    "Serwis uzgodnień porównuje kwoty.",
                    "scale"
                ),
                "PENDING" => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Partial,
                    facts.HasSettlement
                        ? "Ma rozliczenie, czeka na wpłatę od operatora płatności."
                        : "Ma wpłatę od operatora, czeka na rozliczenie kierowcy.",
                    "scale"
                ),
                _ when facts.HasSettlement || facts.HasExternalTransaction => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Waiting,
                    "Serwis uzgodnień zakłada sprawę.",
                    "scale"
                ),
                _ when cancelled => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Later,
                    notApplicable,
                    "scale"
                ),
                _ => new(
                    TrailStepKind.Reconciliation,
                    title,
                    service,
                    TrailState.Later,
                    "Po rozliczeniu kierowcy albo wpłacie od operatora.",
                    "scale"
                ),
            };
        }
    }

    /// <summary>True while some service is expected to answer - the page keeps asking until then.</summary>
    public static bool AwaitsServices(IEnumerable<TrailStep> steps) =>
        steps.Any(s => s.State == TrailState.Waiting);
}

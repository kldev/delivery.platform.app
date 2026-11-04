namespace App.Platform.Portal.Platform;

/// <summary>
/// Where a delivery is on its route. The service's status, read by name - each generated model has its own enum type
/// for it (DeliveryResponse_status, DeliveryStateResponse_status...), all with the same names.
/// </summary>
public enum DeliveryStage
{
    Unknown,
    Created,
    Confirmed,
    Assigned,
    PickedUp,
    InTransit,
    Delivered,
    Cancelled,
}

/// <summary>An action a dispatcher can take on a delivery, with the words the portal uses for it.</summary>
public enum DeliveryAction
{
    Confirm,
    AssignDriver,
    Pickup,
    Start,
    Complete,
    Cancel,
}

public sealed record DeliveryActionInfo(
    DeliveryAction Action,
    string Label,
    string Done,
    string Icon
);

/// <summary>The lifecycle rules of the delivery service, as the portal draws and offers them.</summary>
public static class DeliveryStages
{
    /// <summary>The stops of the route, in order. Cancelled is not a stop: it ends the route where it was.</summary>
    public static IReadOnlyList<DeliveryStage> Route { get; } =
    [
        DeliveryStage.Created,
        DeliveryStage.Confirmed,
        DeliveryStage.Assigned,
        DeliveryStage.PickedUp,
        DeliveryStage.InTransit,
        DeliveryStage.Delivered,
    ];

    // Mirrors Delivery.cancel() in the delivery service.
    private static readonly HashSet<DeliveryStage> Cancellable =
    [
        DeliveryStage.Created,
        DeliveryStage.Assigned,
        DeliveryStage.PickedUp,
        DeliveryStage.InTransit,
    ];

    public static DeliveryStage Parse(string? status) =>
        status switch
        {
            "CREATED" => DeliveryStage.Created,
            "CONFIRMED" => DeliveryStage.Confirmed,
            "ASSIGNED" => DeliveryStage.Assigned,
            "PICKED_UP" => DeliveryStage.PickedUp,
            "IN_TRANSIT" => DeliveryStage.InTransit,
            "DELIVERED" => DeliveryStage.Delivered,
            "CANCELLED" => DeliveryStage.Cancelled,
            _ => DeliveryStage.Unknown,
        };

    public static DeliveryStage From(Enum? status) => Parse(status?.ToString());

    /// <summary>The status name the API takes in a filter.</summary>
    public static string ApiName(DeliveryStage stage) =>
        stage switch
        {
            DeliveryStage.PickedUp => "PICKED_UP",
            DeliveryStage.InTransit => "IN_TRANSIT",
            _ => stage.ToString().ToUpperInvariant(),
        };

    public static string Label(DeliveryStage stage) =>
        stage switch
        {
            DeliveryStage.Created => "Nowa",
            DeliveryStage.Confirmed => "Potwierdzona",
            DeliveryStage.Assigned => "Ma kierowcę",
            DeliveryStage.PickedUp => "Odebrana",
            DeliveryStage.InTransit => "W drodze",
            DeliveryStage.Delivered => "Doręczona",
            DeliveryStage.Cancelled => "Anulowana",
            _ => "Nieznany status",
        };

    public static string Icon(DeliveryStage stage) =>
        stage switch
        {
            DeliveryStage.Created => "file-text",
            DeliveryStage.Confirmed => "circle-check",
            DeliveryStage.Assigned => "user-check",
            DeliveryStage.PickedUp => "package",
            DeliveryStage.InTransit => "truck",
            DeliveryStage.Delivered => "package-check",
            DeliveryStage.Cancelled => "ban",
            _ => "circle-help",
        };

    /// <summary>The colour family of a status pill (see .pill.tone-* in app.css).</summary>
    public static string Tone(DeliveryStage stage) =>
        stage switch
        {
            DeliveryStage.Delivered => "route",
            DeliveryStage.Cancelled => "stop",
            DeliveryStage.PickedUp or DeliveryStage.InTransit => "signal",
            DeliveryStage.Confirmed or DeliveryStage.Assigned => "info",
            _ => "",
        };

    /// <summary>0 for the first stop, 1 for the last; how far the vehicle is drawn on the route line.</summary>
    public static double Progress(DeliveryStage stage)
    {
        var index = IndexOf(stage);
        return index < 0 ? 0 : (double)index / (Route.Count - 1);
    }

    public static int IndexOf(DeliveryStage stage)
    {
        for (var i = 0; i < Route.Count; i++)
            if (Route[i] == stage)
                return i;
        return -1;
    }

    public static bool CanCancel(DeliveryStage stage) => Cancellable.Contains(stage);

    /// <summary>
    /// The one action that moves the delivery to its next stop, if a dispatcher takes it. A confirmed delivery has none:
    /// it moves on when its payment is accepted (the delivery service then assigns a free driver itself).
    /// </summary>
    public static DeliveryActionInfo? NextAction(DeliveryStage stage) =>
        stage switch
        {
            DeliveryStage.Created => Info(DeliveryAction.Confirm),
            DeliveryStage.Assigned => Info(DeliveryAction.Pickup),
            DeliveryStage.PickedUp => Info(DeliveryAction.Start),
            DeliveryStage.InTransit => Info(DeliveryAction.Complete),
            _ => null,
        };

    public static DeliveryActionInfo Info(DeliveryAction action) =>
        action switch
        {
            DeliveryAction.Confirm => new(
                action,
                "Potwierdź zlecenie",
                "Zlecenie potwierdzone",
                "circle-check"
            ),
            DeliveryAction.AssignDriver => new(
                action,
                "Przydziel kierowcę",
                "Kierowca przydzielony",
                "user-check"
            ),
            DeliveryAction.Pickup => new(
                action,
                "Oznacz odbiór paczki",
                "Paczka odebrana",
                "package"
            ),
            DeliveryAction.Start => new(action, "Wyślij w drogę", "Dostawa w drodze", "truck"),
            DeliveryAction.Complete => new(
                action,
                "Potwierdź doręczenie",
                "Dostawa doręczona",
                "package-check"
            ),
            DeliveryAction.Cancel => new(action, "Anuluj dostawę", "Dostawa anulowana", "ban"),
            _ => throw new ArgumentOutOfRangeException(nameof(action), action, null),
        };
}

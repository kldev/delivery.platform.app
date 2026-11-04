namespace Delivery.Platform.PaymentService.Events.Contracts;

// ReSharper disable once ClassNeverInstantiated.Global
public sealed record SettlementCreated(Guid SettlementId, Guid DriverId);
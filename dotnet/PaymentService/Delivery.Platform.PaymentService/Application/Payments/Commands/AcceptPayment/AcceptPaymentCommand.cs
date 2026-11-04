namespace Delivery.Platform.PaymentService.Application.Payments.Commands.AcceptPayment;

public sealed record AcceptPaymentCommand(Guid PaymentId);
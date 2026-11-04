namespace Delivery.Platform.PaymentService.Application.Payments.Commands.DeclinePayment;

public sealed record DeclinePaymentCommand(Guid PaymentId);

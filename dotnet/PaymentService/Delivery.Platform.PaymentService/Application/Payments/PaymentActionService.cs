using Delivery.Platform.PaymentService.Application.Payments.Commands.AcceptPayment;
using Delivery.Platform.PaymentService.Application.Payments.Commands.DeclinePayment;

namespace Delivery.Platform.PaymentService.Application.Payments;

public sealed class PaymentActionService(
    AcceptPaymentHandler acceptPaymentHandler,
    DeclinePaymentHandler declinePaymentHandler)
{
    public async Task Execute(
        Guid paymentId,
        PaymentAction action,
        CancellationToken ct)
    {
        switch (action)
        {
            case PaymentAction.accept:
                await acceptPaymentHandler.Handle(
                    new AcceptPaymentCommand(paymentId),
                    ct);
                break;

            case PaymentAction.decline:
                await declinePaymentHandler.Handle(
                    new DeclinePaymentCommand(paymentId),
                    ct);
                break;

            default:
                throw new ArgumentOutOfRangeException(nameof(action), action, null);
        }
    }
}
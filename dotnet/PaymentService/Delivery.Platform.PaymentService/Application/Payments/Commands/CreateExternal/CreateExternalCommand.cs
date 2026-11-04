namespace Delivery.Platform.PaymentService.Application.Payments.Commands.CreateExternal;

public record CreateExternalCommand(Guid PaymentId, string TransactionId,decimal Amount, string Provider)
{
    
}
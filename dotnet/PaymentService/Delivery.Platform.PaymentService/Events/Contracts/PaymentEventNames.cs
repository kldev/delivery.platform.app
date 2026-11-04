namespace Delivery.Platform.PaymentService.Events.Contracts;

public struct PaymentEventNames
{
    public const string Declined = "payment.declined";
    public const string Paid = "payment.paid";
    public const string Completed = "payment.completed";
}
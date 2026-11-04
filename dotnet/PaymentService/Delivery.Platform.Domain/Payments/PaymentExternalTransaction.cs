namespace Delivery.Platform.Domain.Payments;

public class PaymentExternalTransaction
{
    private PaymentExternalTransaction()
    {
    }

    public PaymentExternalTransaction(
        Guid id,
        Guid paymentId,
        string externalTransactionId,
        string provider,
        decimal amount )
    {
        Id = id;
        PaymentId = paymentId;
        ExternalTransactionId = externalTransactionId;
        Provider = provider;
        CreatedAt = DateTimeOffset.UtcNow;
        Amount = amount;
    }

    public Guid Id { get; private set; }

    public Guid PaymentId { get; private set; }

    public string ExternalTransactionId { get; private set; } = null!;

    public string Provider { get; private set; } = null!;

    public DateTimeOffset CreatedAt { get; private set; }
    
    public decimal Amount { get; private set; }
    
    // ef navigation
    public Payment Payment { get; private set; } = null!;
}
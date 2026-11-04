namespace Delivery.Platform.Domain.Payments;

public class Payment
{
    private Payment()
    {
    }

    public Payment(
        Guid id,
        Guid deliveryId,
        decimal amount,
        string currency)
    {
        Id = id;
        DeliveryId = deliveryId;
        Amount = amount;
        Currency = currency;
        Status = PaymentStatus.Pending;
        CreatedAt = DateTimeOffset.UtcNow;
    }

    public Guid Id { get; private set; }

    public Guid DeliveryId { get; private set; }

    public decimal Amount { get; private set; }

    public string Currency { get; private set; } = null!;

    public PaymentStatus Status { get; private set; }

    public DateTimeOffset CreatedAt { get; private set; }

    public DateTimeOffset? PaidAt { get; private set; }
    
    public ICollection<PaymentExternalTransaction> ExternalTransactions { get; private set; }
        = new List<PaymentExternalTransaction>();

    public void MarkAsPaid()
    {
        if (Status != PaymentStatus.Pending)
        {
            throw new InvalidOperationException(
                $"Payment '{Id}' cannot be marked as paid from status '{Status}'.");
        }
        
        Status = PaymentStatus.Paid;
        PaidAt = DateTimeOffset.UtcNow;
    }

    public void MarkAsFailed()
    {
        if (Status != PaymentStatus.Pending)
        {
            throw new InvalidOperationException(
                $"Payment '{Id}' cannot be marked as failed from status '{Status}'.");
        }
        
        Status = PaymentStatus.Failed;
    }
}
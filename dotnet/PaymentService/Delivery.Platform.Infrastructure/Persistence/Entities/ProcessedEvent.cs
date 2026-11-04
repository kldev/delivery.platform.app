namespace Delivery.Platform.Infrastructure.Persistence;

public sealed class ProcessedEvent
{
    private ProcessedEvent()
    {
    }

    public ProcessedEvent(
        Guid eventId,
        string eventType)
    {
        EventId = eventId;
        EventType = eventType;
        ProcessedAt = DateTimeOffset.UtcNow;
    }

    public Guid EventId { get; private set; }

    public string EventType { get; private set; } = null!;

    public DateTimeOffset ProcessedAt { get; private set; }
}
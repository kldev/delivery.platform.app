namespace Delivery.Platform.Domain.Events;

public interface IEvent
{
    Guid EventId { get; }
    DateTimeOffset OccurredAt { get; }
    Guid AggregateId { get; }
    String Module { get; }
    String EventType { get; }
}
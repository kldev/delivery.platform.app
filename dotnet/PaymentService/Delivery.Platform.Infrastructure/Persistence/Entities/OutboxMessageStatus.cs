namespace Delivery.Platform.Infrastructure.Persistence.Outbox;

public enum OutboxMessageStatus
{
    Pending = 1,
    Published = 2,
    Dead = 3,
}
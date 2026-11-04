using Delivery.Platform.Infrastructure.Persistence.Outbox;

namespace Delivery.Platform.Infrastructure.Port;

public interface IOutboxRepository
{
    Task<IReadOnlyList<OutboxMessage>> ClaimPending(
        DateTimeOffset now,
        DateTimeOffset lockedUntil,
        int limit,
        CancellationToken cancellationToken = default);

    Task MarkPublished(
        Guid id,
        DateTimeOffset publishedAt,
        CancellationToken cancellationToken = default);

    Task MarkFailed(
        Guid id,
        DateTimeOffset nextAttemptAt,
        string error,
        CancellationToken cancellationToken = default);

    Task MarkDead(
        Guid id,
        string error,
        CancellationToken cancellationToken = default);

    Task<OutboxMessage?> FindByEventId(
        Guid eventId,
        CancellationToken cancellationToken = default);
}
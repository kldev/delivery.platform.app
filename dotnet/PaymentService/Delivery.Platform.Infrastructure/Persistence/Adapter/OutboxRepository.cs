using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.Infrastructure.Port;
using Microsoft.EntityFrameworkCore;

namespace Delivery.Platform.Infrastructure.Persistence.Adapter;

public sealed class OutboxRepository(
    PaymentDbContext dbContext) : IOutboxRepository
{
    
    public async Task<IReadOnlyList<OutboxMessage>> ClaimPending(
        DateTimeOffset now,
        DateTimeOffset lockedUntil,
        int limit,
        CancellationToken cancellationToken = default)
    {
        var sql = """
            UPDATE outbox_messages
            SET locked_until = {1}
            WHERE id IN (
                SELECT id
                FROM outbox_messages
                WHERE status = 'PENDING'
                  AND next_attempt_at <= {0}
                  AND (
                      locked_until IS NULL
                      OR locked_until < {0}
                  )
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT {2}
            )
            RETURNING *
            """;

        var entities = await dbContext.OutboxMessages
            .FromSqlRaw(
                sql,
                now,
                lockedUntil,
                limit)
            .AsNoTracking()
            .ToListAsync(cancellationToken);

        return entities
            .ToList();
    }

    public async Task MarkPublished(
        Guid id,
        DateTimeOffset publishedAt,
        CancellationToken cancellationToken = default)
    {
        await dbContext.OutboxMessages
            .Where(x => x.Id == id)
            .ExecuteUpdateAsync(
                setters => setters
                    .SetProperty(
                        x => x.Status,
                        OutboxMessageStatus.Published)
                    .SetProperty(
                        x => x.PublishedAt,
                        publishedAt)
                    .SetProperty(
                        x => x.LockedUntil,
                        (DateTimeOffset?)null),
                cancellationToken);
    }

    public async Task MarkFailed(
        Guid id,
        DateTimeOffset nextAttemptAt,
        string error,
        CancellationToken cancellationToken = default)
    {
        await dbContext.OutboxMessages
            .Where(x => x.Id == id)
            .ExecuteUpdateAsync(
                setters => setters
                    .SetProperty(
                        x => x.Attempts,
                        x => x.Attempts + 1)
                    .SetProperty(
                        x => x.NextAttemptAt,
                        nextAttemptAt)
                    .SetProperty(
                        x => x.LastError,
                        error)
                    .SetProperty(
                        x => x.LockedUntil,
                        (DateTimeOffset?)null),
                cancellationToken);
    }

    public async Task MarkDead(
        Guid id,
        string error,
        CancellationToken cancellationToken = default)
    {
        await dbContext.OutboxMessages
            .Where(x => x.Id == id)
            .ExecuteUpdateAsync(
                setters => setters
                    .SetProperty(
                        x => x.LastError,
                        error)
                    .SetProperty(
                        x => x.Attempts,
                        x => x.Attempts + 1)
                    .SetProperty(
                        x => x.LockedUntil,
                        (DateTimeOffset?)null)
                    .SetProperty(
                        x => x.Status,
                        OutboxMessageStatus.Dead),
                cancellationToken);
    }

    public async Task<OutboxMessage?> FindByEventId(
        Guid eventId,
        CancellationToken cancellationToken = default)
    {
        var entity = await dbContext.OutboxMessages
            .AsNoTracking()
            .FirstOrDefaultAsync(
                x => x.EventId == eventId,
                cancellationToken);

        return entity;
    }
}


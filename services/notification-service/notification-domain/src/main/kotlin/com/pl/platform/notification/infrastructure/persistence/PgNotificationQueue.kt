package com.pl.platform.notification.infrastructure.persistence

import com.pl.platform.notification.application.port.NotificationQueue
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationId
import com.pl.platform.notification.domain.NotificationRecipient
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ApplicationScoped
class PgNotificationQueue(
    private val pool: Pool,
    private val rows: NotificationRows,
) : NotificationQueue {
    // The attempt is counted when it starts (here), not again when it fails.
    override fun claim(
        batchSize: Int,
        lease: Duration,
    ): Uni<List<Notification>> =
        pool
            .preparedQuery(
                """
                UPDATE notifications
                SET status = 'PROCESSING', attempts = attempts + 1, claimed_at = now()
                WHERE id IN (
                    SELECT id
                    FROM notifications
                    WHERE status = 'PENDING'
                       OR (status = 'PROCESSING' AND claimed_at < now() - make_interval(secs => $2))
                    ORDER BY created_at, id
                    LIMIT $1
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING ${NotificationRows.COLUMNS}
                """.trimIndent(),
            ).execute(Tuple.of(batchSize, lease.toSeconds().toDouble()))
            .map { result -> result.map(rows::toNotification) }

    override fun saveRecipient(
        id: NotificationId,
        recipient: NotificationRecipient,
    ): Uni<Void> =
        pool
            .preparedQuery("UPDATE notifications SET recipient = $2::jsonb WHERE id = $1")
            .execute(Tuple.of(id.value, rows.recipientJson(recipient)))
            .replaceWithVoid()

    override fun markSent(id: NotificationId): Uni<Void> =
        pool
            .preparedQuery("UPDATE notifications SET status = 'SENT', sent_at = $2, last_error = NULL WHERE id = $1")
            .execute(Tuple.of(id.value, OffsetDateTime.now(ZoneOffset.UTC)))
            .replaceWithVoid()

    override fun markFailed(
        id: NotificationId,
        error: String,
    ): Uni<Void> =
        pool
            .preparedQuery(
                """
                UPDATE notifications
                SET status = CASE WHEN attempts >= $3 THEN 'FAILED' ELSE 'PENDING' END,
                    last_error = $2
                WHERE id = $1
                """.trimIndent(),
            ).execute(Tuple.of(id.value, error.take(4000), Notification.MAX_ATTEMPTS))
            .replaceWithVoid()
}

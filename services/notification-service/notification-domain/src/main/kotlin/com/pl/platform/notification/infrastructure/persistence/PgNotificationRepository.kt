package com.pl.platform.notification.infrastructure.persistence

import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.application.port.Transaction
import com.pl.platform.notification.domain.ConflictException
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationId
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.Tuple
import io.vertx.pgclient.PgException
import jakarta.enterprise.context.ApplicationScoped
import java.time.ZoneOffset

@ApplicationScoped
class PgNotificationRepository(
    private val pool: Pool,
    private val rows: NotificationRows,
) : NotificationRepository {
    override fun insert(notification: Notification): Uni<Notification> = insert(pool, notification)

    override fun insert(
        transaction: Transaction,
        notification: Notification,
    ): Uni<Notification> = insert(transaction.connection, notification)

    private fun insert(
        client: SqlClient,
        notification: Notification,
    ): Uni<Notification> =
        client
            .preparedQuery(
                """
                INSERT INTO notifications (${NotificationRows.COLUMNS})
                VALUES ($1, $2, $3, $4::jsonb, $5, $6::jsonb, $7, $8, $9, $10, $11)
                """.trimIndent(),
            ).execute(
                Tuple
                    .tuple()
                    .addUUID(notification.id.value)
                    .addUUID(notification.eventId)
                    .addString(notification.eventType)
                    .addString(rows.recipientJson(notification.recipient))
                    .addString(notification.channel.name)
                    .addString(notification.payload)
                    .addString(notification.status.name)
                    .addInteger(notification.attempts)
                    .addString(notification.lastError)
                    .addOffsetDateTime(notification.createdAt.atOffset(ZoneOffset.UTC))
                    .addOffsetDateTime(notification.sentAt?.atOffset(ZoneOffset.UTC)),
            ).replaceWith(notification)
            .onFailure { it is PgException && it.sqlState == UNIQUE_VIOLATION }
            .transform {
                ConflictException(
                    "notification.duplicate",
                    "Event ${notification.eventId} already has a ${notification.channel} notification",
                )
            }

    override fun findById(id: NotificationId): Uni<Notification?> =
        pool
            .preparedQuery("SELECT ${NotificationRows.COLUMNS} FROM notifications WHERE id = $1")
            .execute(Tuple.of(id.value))
            .map { result -> result.firstOrNull()?.let(rows::toNotification) }

    private companion object {
        const val UNIQUE_VIOLATION = "23505"
    }
}

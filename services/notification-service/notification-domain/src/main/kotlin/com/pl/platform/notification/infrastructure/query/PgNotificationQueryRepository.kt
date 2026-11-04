package com.pl.platform.notification.infrastructure.query

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.notification.application.listnotifications.ListNotificationsQuery
import com.pl.platform.notification.application.port.NotificationQueryRepository
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.infrastructure.persistence.NotificationRows
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import java.time.ZoneOffset

@ApplicationScoped
class PgNotificationQueryRepository(
    private val pool: Pool,
    private val rows: NotificationRows,
) : NotificationQueryRepository {
    override fun find(query: ListNotificationsQuery): Uni<SliceResponse<Notification>> {
        val conditions = mutableListOf<String>()
        val parameters = Tuple.tuple()
        fun param(): String = "$" + parameters.size()

        query.status?.let {
            parameters.addString(it.name)
            conditions += "status = ${param()}"
        }
        query.eventId?.let {
            parameters.addUUID(it)
            conditions += "event_id = ${param()}"
        }
        query.fromAtInstant()?.let {
            parameters.addOffsetDateTime(it.atOffset(ZoneOffset.UTC))
            conditions += "created_at >= ${param()}"
        }
        query.toAtInstant()?.let {
            parameters.addOffsetDateTime(it.atOffset(ZoneOffset.UTC))
            conditions += "created_at < ${param()}"
        }
        val where = if (conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"

        val size = query.page.size
        parameters.addLong(size.toLong() + 1)
        val limit = param()
        parameters.addLong(query.page.page.toLong() * size)
        val offset = param()

        return pool
            .preparedQuery(
                """
                SELECT ${NotificationRows.COLUMNS}
                FROM notifications
                $where
                ORDER BY created_at DESC, id DESC
                LIMIT $limit OFFSET $offset
                """.trimIndent(),
            ).execute(parameters)
            .map { result ->
                val items = result.map(rows::toNotification)
                SliceResponse(content = items.take(size), hasNext = items.size > size)
            }
    }
}

package com.pl.platform.notification.infrastructure.messaging.inbox

import com.pl.platform.notification.application.port.ProcessedEvents
import com.pl.platform.notification.application.port.Transaction
import com.pl.platform.notification.infrastructure.persistence.connection
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PgProcessedEvents : ProcessedEvents {
    override fun markProcessed(
        transaction: Transaction,
        eventId: UUID,
        eventType: String,
    ): Uni<Boolean> =
        transaction.connection
            .preparedQuery(
                """
                INSERT INTO processed_events (event_id, event_type, processed_at)
                VALUES ($1, $2, CURRENT_TIMESTAMP)
                ON CONFLICT (event_id) DO NOTHING
                """.trimIndent(),
            ).execute(Tuple.of(eventId, eventType))
            .map { it.rowCount() == 1 }
}

package com.pl.platform.notification.infrastructure.persistence

import com.pl.platform.notification.application.port.BackgroundTaskStats
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@ApplicationScoped
class PgBackgroundTaskStats(
    private val pool: Pool
) : BackgroundTaskStats {

    override fun recordExecution(
        taskName: String,
        durationMs: Long,
        error: Throwable?
    ): Uni<Void> {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        val sql = """
            INSERT INTO background_task_stats (               
                task_name,
                executed_times,
                last_execution_at,
                last_execution_duration_ms,
                last_success_at,
                last_error,
                created_at,
                updated_at,
                id
            )
            VALUES (
                $1,
                1,
                $2,
                $3,
                $4,
                $5,
                $2,
                $2,
                $6
            )
            ON CONFLICT (task_name)
            DO UPDATE SET
                executed_times = background_task_stats.executed_times + 1,
                last_execution_at = EXCLUDED.last_execution_at,
                last_execution_duration_ms = EXCLUDED.last_execution_duration_ms,
                last_success_at = EXCLUDED.last_success_at,
                last_error = EXCLUDED.last_error,
                updated_at = EXCLUDED.updated_at
        """.trimIndent()

        return pool.preparedQuery(sql)
            .execute(
                Tuple.of(
                    taskName,
                    now,
                    durationMs,
                    if (error == null) now else null,
                    error?.message
                ).addUUID(UUID.randomUUID())
            )
            .replaceWithVoid()
    }
}
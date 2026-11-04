package com.pl.platform.delivery.infrastructure.idempotency

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/** Deletes expired idempotency records in batches; same criterion as the TTL (expires_at). */
@Component
class IdempotencyCleaner(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${idempotency.cleaner.cron:0 0 */3 * * *}")
    fun cleanup() {
        var total = 0
        do {
            val deleted =
                jdbcTemplate.update(
                    """
                    DELETE FROM idempotency_records
                    WHERE ctid IN (SELECT ctid FROM idempotency_records WHERE expires_at < now() LIMIT $BATCH_SIZE)
                    """.trimIndent(),
                )
            total += deleted
        } while (deleted == BATCH_SIZE)
        if (total > 0) log.info("Deleted {} expired idempotency records", total)
    }

    private companion object {
        const val BATCH_SIZE = 1000
    }
}

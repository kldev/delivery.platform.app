package com.pl.platform.delivery.testing

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

/** Reads stored state with SQL, so assertions do not depend on the JPA mapping under test. */
@Component
class DeliveryStateFixture(
    private val jdbc: JdbcTemplate,
) {
    fun statusOf(deliveryId: UUID): String =
        jdbc.queryForObject("SELECT status FROM deliveries WHERE id = ?", String::class.java, deliveryId)!!

    fun driverOf(deliveryId: UUID): UUID? =
        jdbc.queryForObject("SELECT driver_id FROM deliveries WHERE id = ?", UUID::class.java, deliveryId)

    fun outboxEventTypes(aggregateId: UUID): List<String> =
        jdbc.queryForList(
            "SELECT event_type FROM outbox_messages WHERE aggregate_id = ? ORDER BY created_at",
            String::class.java,
            aggregateId,
        ).filterNotNull()

    fun outboxPayloads(aggregateId: UUID): List<String> =
        jdbc.queryForList(
            "SELECT payload::text FROM outbox_messages WHERE aggregate_id = ? ORDER BY created_at",
            String::class.java,
            aggregateId,
        ).filterNotNull()

    fun isProcessed(eventId: UUID): Boolean =
        jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?", Long::class.java, eventId)!! > 0
}

package com.pl.platform.settlement.infrastructure.messaging

import com.pl.platform.common.messaging.event.delivery.DeliveryCompletedEvent
import com.pl.platform.settlement.testing.BaseIntegrationTest
import com.pl.platform.settlement.application.deliverycompleted.DeliveryCompletedHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

/** The listener is not a bean in tests (no broker); built here around the real handler. */
class DeliveryEventsListenerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var jsonMapper: JsonMapper

    @Autowired
    lateinit var handler: DeliveryCompletedHandler

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Test
    fun `the completion time is the event's occurredAt, not the time it is processed`() {
        val deliveryId = UUID.randomUUID()
        // 23:30 on a Wednesday in Warsaw: night bonus. Processing happens now, which is not that time.
        val record = record(deliveryId, occurredAt = "2026-11-04T22:30:00Z")

        DeliveryEventsListener(jsonMapper, handler).onDeliveryCompleted(record)

        val row = jdbc.queryForMap("SELECT completed_at, night_percentage FROM settlements WHERE delivery_id = ?", deliveryId)
        assertThat((row["completed_at"] as Timestamp).toInstant()).isEqualTo(Instant.parse("2026-11-04T22:30:00Z"))
        assertThat(row["night_percentage"] as BigDecimal).isEqualByComparingTo("5")
    }

    @Test
    fun `a redelivered record keeps its eventId`() {
        val record = record(UUID.randomUUID(), occurredAt = "2026-11-04T11:00:00Z")
        val listener = DeliveryEventsListener(jsonMapper, handler)

        listener.onDeliveryCompleted(record)
        listener.onDeliveryCompleted(record)

        val eventId = jsonMapper.readTree(record.value()).get("eventId").asString()
        assertThat(jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?::uuid", Int::class.java, eventId))
            .isEqualTo(1)
    }

    // The JSON delivery-service writes: its outbox serializes the whole event, occurredAt included.
    private fun record(
        deliveryId: UUID,
        occurredAt: String,
    ): ConsumerRecord<String, String> {
        val event = DeliveryCompletedEvent(deliveryId, UUID.randomUUID(), "Ada Lovelace", BigDecimal("20"), BigDecimal("100.00"))
        val json = jsonMapper.valueToTree<ObjectNode>(event).put("occurredAt", occurredAt).toString()
        return ConsumerRecord("delivery.completed", 0, 0, null, json)
    }
}

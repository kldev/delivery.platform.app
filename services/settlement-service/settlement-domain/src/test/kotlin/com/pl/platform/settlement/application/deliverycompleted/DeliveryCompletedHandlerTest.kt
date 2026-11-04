package com.pl.platform.settlement.application.deliverycompleted

import com.pl.platform.settlement.testing.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class DeliveryCompletedHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: DeliveryCompletedHandler

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @BeforeEach
    fun clean() {
        jdbc.execute("TRUNCATE TABLE settlements, outbox_messages, processed_events")
    }

    @Test
    fun `settles the delivery and announces the driver's share to the ledger`() {
        val deliveryId = UUID.randomUUID()

        handler.handle(command(deliveryId, price = "100.00"))

        assertThat(jdbc.queryForObject("SELECT driver_amount FROM settlements WHERE delivery_id = ?", BigDecimal::class.java, deliveryId))
            .isEqualByComparingTo("70.00")
        val events = outbox()
        assertThat(events.keys).containsExactlyInAnyOrder("settlement.created", "driver.settlement.completed")
        // Before: the ledger was told the full price of the delivery (100.00), not what the driver earned.
        assertThat(events["driver.settlement.completed"]).contains("\"amount\": 70.00")
    }

    @Test
    fun `the same event twice settles once`() {
        val command = command(UUID.randomUUID())

        handler.handle(command)
        handler.handle(command)

        assertThat(jdbc.queryForObject("SELECT count(*) FROM settlements", Int::class.java)).isEqualTo(1)
        assertThat(outbox()).hasSize(2)
    }

    @Test
    fun `another event for an already settled delivery is skipped and recorded`() {
        val deliveryId = UUID.randomUUID()
        handler.handle(command(deliveryId))
        val second = command(deliveryId)

        handler.handle(second)

        assertThat(jdbc.queryForObject("SELECT count(*) FROM settlements", Int::class.java)).isEqualTo(1)
        assertThat(jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?", Int::class.java, second.eventId))
            .isEqualTo(1)
    }

    private fun outbox(): Map<String, String> =
        jdbc
            .queryForList("SELECT event_type, payload::text AS payload FROM outbox_messages")
            .associate { it["event_type"] as String to it["payload"] as String }

    private fun command(
        deliveryId: UUID,
        price: String = "100.00",
    ) = DeliveryCompletedCommand(
        eventId = UUID.randomUUID(),
        eventType = "delivery.completed",
        deliveryId = deliveryId,
        driverId = UUID.randomUUID(),
        driverFullName = "Ada Lovelace",
        deliveryAmount = BigDecimal(price),
        currency = "PLN",
        distanceKm = BigDecimal("20"),
        completedAt = Instant.parse("2026-11-04T11:00:00Z"), // weekday noon in Warsaw: base rate only
    )
}

package com.pl.platform.reconciliation.infrastructure.messaging

import com.pl.platform.common.messaging.event.payments.PaymentCompletedEvent
import com.pl.platform.common.messaging.event.settlement.SettlementCreatedEvent
import com.pl.platform.reconciliation.application.paymentcompleted.PaymentCompletedHandler
import com.pl.platform.reconciliation.application.settlementcreated.SettlementCreatedHandler
import com.pl.platform.reconciliation.testing.BaseIntegrationTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Listeners are not beans in tests (no broker); built here around the real handlers. */
class EventListenersTest : BaseIntegrationTest() {
    @Autowired
    lateinit var jsonMapper: JsonMapper

    @Autowired
    lateinit var settlements: SettlementCreatedHandler

    @Autowired
    lateinit var payments: PaymentCompletedHandler

    @Test
    fun `a redelivered record keeps its eventId and is processed once`() {
        val deliveryId = UUID.randomUUID()
        val settlement = jsonMapper.writeValueAsString(settlementCreated(deliveryId, settlementId = UUID.randomUUID()))
        val listener = SettlementEventsListener(jsonMapper, settlements)

        listener.onSettlementCreated(ConsumerRecord("settlement.created", 0, 0, null, settlement))
        listener.onSettlementCreated(ConsumerRecord("settlement.created", 0, 0, null, settlement))

        val processed = jdbcTemplate.queryForObject("SELECT count(*) FROM processed_events", Int::class.java)
        val eventId = jsonMapper.readTree(settlement).get("eventId").asString()
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM processed_events WHERE event_id = ?::uuid", Int::class.java, eventId))
            .isEqualTo(1)
        assertThat(processed).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `a payment of the delivery's price reconciles the delivery`() {
        val deliveryId = UUID.randomUUID()

        settlementArrives(deliveryId)
        paymentArrives(deliveryId, BigDecimal("100.00"))

        assertThat(statusOf(deliveryId)).isEqualTo("RECONCILED")
    }

    @Test
    fun `a payment of only the driver's share is a discrepancy`() {
        // The settlement gives the driver 70.00 of the delivery's 100.00: the customer still owes the whole price.
        val deliveryId = UUID.randomUUID()

        settlementArrives(deliveryId)
        paymentArrives(deliveryId, BigDecimal("70.00"))

        assertThat(statusOf(deliveryId)).isEqualTo("DISCREPANCY")
        assertThat(jdbcTemplate.queryForObject("SELECT difference FROM reconciliations WHERE delivery_id = ?", BigDecimal::class.java, deliveryId))
            .isEqualByComparingTo("-30.00")
    }

    private fun settlementArrives(deliveryId: UUID) =
        SettlementEventsListener(jsonMapper, settlements).onSettlementCreated(
            ConsumerRecord("settlement.created", 0, 0, null, jsonMapper.writeValueAsString(settlementCreated(deliveryId))),
        )

    private fun paymentArrives(
        deliveryId: UUID,
        amount: BigDecimal,
    ) = PaymentEventsListener(jsonMapper, payments).onPaymentCompleted(
        ConsumerRecord(
            "payment.completed",
            0,
            0,
            null,
            jsonMapper.writeValueAsString(PaymentCompletedEvent(UUID.randomUUID(), deliveryId, "TX-1", amount, "PLN")),
        ),
    )

    private fun statusOf(deliveryId: UUID): String? =
        jdbcTemplate.queryForObject("SELECT status FROM reconciliations WHERE delivery_id = ?", String::class.java, deliveryId)

    private fun settlementCreated(
        deliveryId: UUID,
        settlementId: UUID = UUID.randomUUID(),
    ) = SettlementCreatedEvent(
        settlementId = settlementId,
        deliveryId = deliveryId,
        driverId = UUID.randomUUID(),
        deliveryAmount = BigDecimal("100.00"),
        currency = "PLN",
        basePercentage = BigDecimal("0.70"),
        nightPercentage = BigDecimal.ZERO,
        weekendPercentage = BigDecimal.ZERO,
        distancePercentage = BigDecimal.ZERO,
        totalPercentage = BigDecimal("0.70"),
        driverAmount = BigDecimal("70.00"),
        distanceKm = BigDecimal("12.5"),
        completedAt = Instant.parse("2026-11-04T10:00:00Z"),
    )
}

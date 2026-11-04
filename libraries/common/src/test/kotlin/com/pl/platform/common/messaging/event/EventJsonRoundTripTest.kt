package com.pl.platform.common.messaging.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pl.platform.common.messaging.event.delivery.DeliveryAssignedEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryCancelledEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryCompletedEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryConfirmedEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryCreatedEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryPickedUpEvent
import com.pl.platform.common.messaging.event.delivery.DeliveryStartedEvent
import com.pl.platform.common.messaging.event.payments.PaymentCompletedEvent
import com.pl.platform.common.messaging.event.payments.PaymentDeclinedEvent
import com.pl.platform.common.messaging.event.payments.PaymentPaidEvent
import com.pl.platform.common.messaging.event.settlement.DriverSettlementCompletedEvent
import com.pl.platform.common.messaging.event.settlement.SettlementCreatedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * A consumer must read back exactly what the producer wrote, eventId and occurredAt included: consumers
 * deduplicate on eventId and settlement computes bonuses from occurredAt. Before, both were base class
 * defaults and deserializing made up new ones.
 */
class EventJsonRoundTripTest {
    private val jackson3: JsonMapper = JsonMapper.builder().addModule(kotlinModule()).build()
    private val jackson2: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @ParameterizedTest
    @MethodSource("events")
    fun `Jackson 3 (Spring services) reads back every field`(event: Event) {
        val back = jackson3.readValue(jackson3.writeValueAsString(event), event.javaClass)

        assertThat(back).isEqualTo(event)
        assertSameIdentity(back, event)
    }

    @ParameterizedTest
    @MethodSource("events")
    fun `Jackson 2 (Quarkus services) reads back every field`(event: Event) {
        val back = jackson2.readValue(jackson2.writeValueAsString(event), event.javaClass)

        assertThat(back).isEqualTo(event)
        assertSameIdentity(back, event)
    }

    @ParameterizedTest
    @MethodSource("events")
    fun `the event header can be read on its own as EventMetadata`(event: Event) {
        val metadata = jackson3.readValue(jackson3.writeValueAsString(event), EventMetadata::class.java)

        assertSameIdentity(metadata, event)
    }

    private fun assertSameIdentity(
        actual: Event,
        expected: Event,
    ) {
        assertThat(actual.eventId).isEqualTo(expected.eventId)
        assertThat(actual.occurredAt).isEqualTo(expected.occurredAt)
        assertThat(actual.eventType).isEqualTo(expected.eventType)
        assertThat(actual.aggregateId).isEqualTo(expected.aggregateId)
        assertThat(actual.module).isEqualTo(expected.module)
    }

    companion object {
        private val id = UUID.fromString("8bd6afc7-c6ac-4c01-823b-64752244c5f7")
        private val other = UUID.fromString("16cd75aa-5136-46e6-a48d-4453ffd88e6d")
        private val money = BigDecimal("70.00")

        // A fixed eventId/occurredAt in the past: a made-up "now" or random id would not match.
        private val eventId = UUID.fromString("045fd60e-f6cc-40d9-af06-6d20e07066fd")
        private val occurredAt = Instant.parse("2026-11-04T22:30:00Z")

        @JvmStatic
        fun events(): List<Event> =
            listOf(
                DeliveryCreatedEvent(id, money, eventId, occurredAt),
                DeliveryConfirmedEvent(id, money, "PLN", eventId, occurredAt),
                DeliveryAssignedEvent(id, other, eventId, occurredAt),
                DeliveryPickedUpEvent(id, eventId, occurredAt),
                DeliveryStartedEvent(id, eventId, occurredAt),
                DeliveryCompletedEvent(id, other, "Ada Lovelace", BigDecimal("12.5"), money, "PLN", eventId, occurredAt),
                DeliveryCancelledEvent(id, eventId, occurredAt),
                PaymentPaidEvent(id, other, eventId, occurredAt),
                PaymentDeclinedEvent(id, other, eventId, occurredAt),
                PaymentCompletedEvent(other, id, "TX-1", money, "PLN", eventId, occurredAt),
                SettlementCreatedEvent(
                    other, id, other, "Ada", BigDecimal("100.00"), "PLN", BigDecimal("70"), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal("70"), money, BigDecimal("12.5"), occurredAt,
                    eventId, occurredAt,
                ),
                DriverSettlementCompletedEvent(other, other, money, "PLN", "Ada", id, eventId, occurredAt),
                TestEvent(eventId, occurredAt, id, "test"),
            )
    }
}

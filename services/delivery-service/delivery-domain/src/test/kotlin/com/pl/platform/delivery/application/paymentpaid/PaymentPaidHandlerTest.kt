package com.pl.platform.delivery.application.paymentpaid

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import com.pl.platform.delivery.domain.InvalidTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class PaymentPaidHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: PaymentPaidHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @BeforeEach
    fun clean() {
        cleanDatabase() // the driver is picked at random from all drivers
    }

    @Test
    fun `a paid delivery gets a driver and the event is marked as processed`() {
        val id = confirmedDelivery()
        val driverId = drivers.create()
        val command = PaymentPaidCommand(UUID.randomUUID(), "payment.paid", DeliveryId(id))

        handler.handle(command)

        assertThat(state.statusOf(id)).isEqualTo("ASSIGNED")
        assertThat(state.driverOf(id)).isEqualTo(driverId)
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.assigned")
        assertThat(state.isProcessed(command.eventId)).isTrue()
    }

    @Test
    fun `the same event delivered twice is applied once`() {
        val id = confirmedDelivery()
        drivers.create()
        val command = PaymentPaidCommand(UUID.randomUUID(), "payment.paid", DeliveryId(id))

        handler.handle(command)
        handler.handle(command)

        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.assigned")
    }

    @Test
    fun `a failed event is not marked as processed, so a redelivery can apply it`() {
        val id = deliveries.create() // CREATED, so assign is not allowed
        drivers.create()
        val command = PaymentPaidCommand(UUID.randomUUID(), "payment.paid", DeliveryId(id))

        assertThatThrownBy { handler.handle(command) }.isInstanceOf(InvalidTransitionException::class.java)

        assertThat(state.isProcessed(command.eventId)).isFalse()
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }

    private fun confirmedDelivery() = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }
}

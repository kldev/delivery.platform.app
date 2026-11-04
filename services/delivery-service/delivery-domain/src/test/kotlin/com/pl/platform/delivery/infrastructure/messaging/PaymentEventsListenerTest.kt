package com.pl.platform.delivery.infrastructure.messaging

import com.pl.platform.common.messaging.event.payments.PaymentDeclinedEvent
import com.pl.platform.common.messaging.event.payments.PaymentPaidEvent
import com.pl.platform.delivery.application.paymentdeclined.PaymentDeclinedHandler
import com.pl.platform.delivery.application.paymentpaid.PaymentPaidHandler
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

/**
 * A record delivered twice (consumer restart, rebalance) must be applied once. Before the fix in
 * libraries/common the second copy came with a new eventId and failed on the status transition.
 * The listener is not a bean in tests (no broker), so it is built here around the real handlers.
 */
class PaymentEventsListenerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var jsonMapper: JsonMapper

    @Autowired
    lateinit var paymentPaidHandler: PaymentPaidHandler

    @Autowired
    lateinit var paymentDeclinedHandler: PaymentDeclinedHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `a redelivered payment paid record assigns a driver once`() {
        val deliveryId = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }
        drivers.create()
        val record = record("payment.paid", PaymentPaidEvent(deliveryId, UUID.randomUUID()))

        listener().onPaymentPaid(record)
        listener().onPaymentPaid(record)

        assertThat(state.statusOf(deliveryId)).isEqualTo("ASSIGNED")
        assertThat(state.outboxEventTypes(deliveryId)).containsExactly("delivery.assigned")
    }

    @Test
    fun `a redelivered payment declined record cancels once`() {
        val deliveryId = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }
        val record = record("payment.declined", PaymentDeclinedEvent(deliveryId, UUID.randomUUID()))

        listener().onPaymentDeclined(record)
        listener().onPaymentDeclined(record)

        assertThat(state.statusOf(deliveryId)).isEqualTo("CANCELLED")
        assertThat(state.outboxEventTypes(deliveryId)).containsExactly("delivery.cancelled")
    }

    private fun listener() = PaymentEventsListener(jsonMapper, paymentPaidHandler, paymentDeclinedHandler)

    private fun record(
        topic: String,
        event: Any,
    ) = ConsumerRecord<String, String>(topic, 0, 0, null, jsonMapper.writeValueAsString(event))
}

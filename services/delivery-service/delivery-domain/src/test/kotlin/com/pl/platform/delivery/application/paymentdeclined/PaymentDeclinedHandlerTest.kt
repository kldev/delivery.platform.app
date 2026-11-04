package com.pl.platform.delivery.application.paymentdeclined

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class PaymentDeclinedHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: PaymentDeclinedHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `a declined payment cancels the delivery once, even when the event comes twice`() {
        // The payment exists only once the delivery is confirmed: that is when it can be declined.
        val id = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }
        val command = PaymentDeclinedCommand(UUID.randomUUID(), "payment.declined", DeliveryId(id))

        handler.handle(command)
        handler.handle(command)

        assertThat(state.statusOf(id)).isEqualTo("CANCELLED")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.cancelled")
        assertThat(state.isProcessed(command.eventId)).isTrue()
    }
}

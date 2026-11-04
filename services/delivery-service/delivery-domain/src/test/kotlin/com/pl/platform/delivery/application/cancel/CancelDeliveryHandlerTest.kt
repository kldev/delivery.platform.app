package com.pl.platform.delivery.application.cancel

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.domain.InvalidTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CancelDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: CancelDeliveryHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `cancels a created delivery and writes delivery cancelled`() {
        val id = deliveries.create()

        handler.handle(CancelDeliveryCommand(DeliveryId(id)))

        assertThat(state.statusOf(id)).isEqualTo("CANCELLED")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.cancelled")
    }

    @Test
    fun `a delivered delivery cannot be cancelled and nothing is written`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.DELIVERED)

        assertThatThrownBy { handler.handle(CancelDeliveryCommand(DeliveryId(id))) }
            .isInstanceOf(InvalidTransitionException::class.java)

        assertThat(state.statusOf(id)).isEqualTo("DELIVERED")
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }
}

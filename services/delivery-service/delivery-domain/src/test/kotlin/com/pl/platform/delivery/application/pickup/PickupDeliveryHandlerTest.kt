package com.pl.platform.delivery.application.pickup

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import com.pl.platform.delivery.domain.InvalidTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PickupDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: PickupDeliveryHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `picks up an assigned delivery and writes delivery picked up`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.ASSIGNED, driverId = drivers.create())

        handler.handle(PickupDeliveryCommand(DeliveryId(id)))

        assertThat(state.statusOf(id)).isEqualTo("PICKED_UP")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.picked_up")
    }

    @Test
    fun `a delivery that is not assigned cannot be picked up`() {
        val id = deliveries.create()

        assertThatThrownBy { handler.handle(PickupDeliveryCommand(DeliveryId(id))) }
            .isInstanceOf(InvalidTransitionException::class.java)

        assertThat(state.statusOf(id)).isEqualTo("CREATED")
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }
}

package com.pl.platform.delivery.application.start

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StartDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: StartDeliveryHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `starts transit of a picked up delivery and writes delivery started`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.PICKED_UP, driverId = drivers.create())

        handler.handle(StartDeliveryCommand(DeliveryId(id)))

        assertThat(state.statusOf(id)).isEqualTo("IN_TRANSIT")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.started")
    }
}

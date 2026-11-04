package com.pl.platform.delivery.application.complete

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CompleteDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: CompleteDeliveryHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `delivers a delivery in transit and writes delivery completed with the driver`() {
        val id = deliveries.create()
        val driverId = drivers.create(firstName = "Ada", lastName = "Lovelace")
        deliveries.changeStatus(id, DeliveryStatusJpa.IN_TRANSIT, driverId = driverId)

        handler.handle(CompleteDeliveryCommand(DeliveryId(id)))

        assertThat(state.statusOf(id)).isEqualTo("DELIVERED")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.completed")
        assertThat(state.outboxPayloads(id).single())
            .contains(driverId.toString())
            .contains("Ada Lovelace")
    }
}

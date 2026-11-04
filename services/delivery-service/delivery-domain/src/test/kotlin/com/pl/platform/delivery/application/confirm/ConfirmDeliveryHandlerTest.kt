package com.pl.platform.delivery.application.confirm

import com.pl.platform.delivery.domain.NotFoundException
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
import java.util.UUID

class ConfirmDeliveryHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: ConfirmDeliveryHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Test
    fun `confirms a created delivery and writes delivery confirmed with the price`() {
        val id = deliveries.create()

        handler.handle(ConfirmDeliveryCommand(DeliveryId(id)))

        assertThat(state.statusOf(id)).isEqualTo("CONFIRMED")
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.confirmed")
        assertThat(state.outboxPayloads(id).single()).contains("\"price\"")
    }

    @Test
    fun `a delivery that is not CREATED cannot be confirmed and nothing is written`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.CANCELLED)

        assertThatThrownBy { handler.handle(ConfirmDeliveryCommand(DeliveryId(id))) }
            .isInstanceOf(InvalidTransitionException::class.java)

        assertThat(state.statusOf(id)).isEqualTo("CANCELLED")
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }

    @Test
    fun `unknown delivery is reported as not found`() {
        assertThatThrownBy { handler.handle(ConfirmDeliveryCommand(DeliveryId(UUID.randomUUID()))) }
            .isInstanceOf(NotFoundException::class.java)
    }
}

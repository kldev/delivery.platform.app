package com.pl.platform.delivery.application.assigndriver

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.domain.DriverId
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.BaseIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import com.pl.platform.delivery.domain.InvalidTransitionException
import com.pl.platform.delivery.domain.RuleViolationException
import com.pl.platform.delivery.domain.NotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AssignDriverHandlerTest : BaseIntegrationTest() {
    @Autowired
    lateinit var handler: AssignDriverHandler

    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `assigns the given driver to a confirmed delivery and writes delivery assigned`() {
        val id = confirmedDelivery()
        val driverId = drivers.create()

        handler.handle(AssignDriverCommand(DeliveryId(id), DriverId(driverId)))

        assertThat(state.statusOf(id)).isEqualTo("ASSIGNED")
        assertThat(state.driverOf(id)).isEqualTo(driverId)
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.assigned")
        assertThat(state.outboxPayloads(id).single()).contains(driverId.toString())
    }

    @Test
    fun `picks one of the drivers when none is given`() {
        val id = confirmedDelivery()
        val driverIds = setOf(drivers.create(), drivers.create(), drivers.create())

        handler.handle(AssignDriverCommand(DeliveryId(id), driverId = null))

        assertThat(state.driverOf(id)).isIn(driverIds)
        assertThat(state.outboxEventTypes(id)).containsExactly("delivery.assigned")
    }

    @Test
    fun `fails when there is no driver to pick and changes nothing`() {
        val id = confirmedDelivery()

        assertThatThrownBy { handler.handle(AssignDriverCommand(DeliveryId(id), driverId = null)) }
            .isInstanceOf(RuleViolationException::class.java)
            .hasFieldOrPropertyWithValue("code", "driver.none-available")

        assertThat(state.statusOf(id)).isEqualTo("CONFIRMED")
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }

    @Test
    fun `a delivery that is not confirmed cannot be assigned and nothing is written`() {
        val id = deliveries.create()
        val driverId = drivers.create()

        assertThatThrownBy { handler.handle(AssignDriverCommand(DeliveryId(id), DriverId(driverId))) }
            .isInstanceOf(InvalidTransitionException::class.java)

        assertThat(state.statusOf(id)).isEqualTo("CREATED")
        assertThat(state.driverOf(id)).isNull()
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }

    @Test
    fun `an unknown driver is reported as not found and nothing is written`() {
        val id = confirmedDelivery()

        assertThatThrownBy { handler.handle(AssignDriverCommand(DeliveryId(id), DriverId(UUID.randomUUID()))) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("code", "driver.not-found")

        assertThat(state.statusOf(id)).isEqualTo("CONFIRMED")
        assertThat(state.outboxEventTypes(id)).isEmpty()
    }

    private fun confirmedDelivery() = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }
}

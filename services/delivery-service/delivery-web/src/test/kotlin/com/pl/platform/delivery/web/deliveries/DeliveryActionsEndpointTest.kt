package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.BaseRestIntegrationTest
import com.pl.platform.delivery.infrastructure.messaging.outbox.InMemoryEventPublisher
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DeliveryStateFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.util.UUID
import java.util.concurrent.TimeUnit

class DeliveryActionsEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @Autowired
    lateinit var state: DeliveryStateFixture

    @Autowired
    lateinit var eventPublisher: InMemoryEventPublisher

    @BeforeEach
    fun clean() {
        eventPublisher.clear()
    }

    @Test
    fun `confirm returns the new state and publishes delivery confirmed`() {
        val id = deliveries.create()

        action(id, "confirm")
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id.toString())
            .jsonPath("$.status")
            .isEqualTo("CONFIRMED")

        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            assertThat(eventPublisher.events()).anyMatch { it.eventType == "delivery.confirmed" && it.aggregateId == id }
        }
    }

    @Test
    fun `assign-driver with a driver id assigns that driver`() {
        val id = confirmed()
        val driverId = drivers.create()

        restTestClient
            .post()
            .uri("/api/deliveries/$id/assign-driver")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"driverId":"$driverId"}""")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("ASSIGNED")
            .jsonPath("$.driverId")
            .isEqualTo(driverId.toString())
    }

    @Test
    fun `assign-driver without a body picks a driver`() {
        val id = confirmed()
        drivers.create()

        action(id, "assign-driver")
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("ASSIGNED")
            .jsonPath("$.driverId")
            .exists()
    }

    @Test
    fun `assign-driver with an unknown driver is a 404`() {
        val id = confirmed()

        restTestClient
            .post()
            .uri("/api/deliveries/$id/assign-driver")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"driverId":"${UUID.randomUUID()}"}""")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("driver.not-found")
    }

    @Test
    fun `pickup, start and complete walk the delivery to DELIVERED`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.ASSIGNED, driverId = drivers.create())

        action(id, "pickup").expectStatus().isOk.expectBody().jsonPath("$.status").isEqualTo("PICKED_UP")
        action(id, "start").expectStatus().isOk.expectBody().jsonPath("$.status").isEqualTo("IN_TRANSIT")
        action(id, "complete").expectStatus().isOk.expectBody().jsonPath("$.status").isEqualTo("DELIVERED")

        assertThat(state.outboxEventTypes(id))
            .containsExactly("delivery.picked_up", "delivery.started", "delivery.completed")
    }

    @Test
    fun `cancel cancels a created delivery`() {
        val id = deliveries.create()

        action(id, "cancel").expectStatus().isOk.expectBody().jsonPath("$.status").isEqualTo("CANCELLED")
    }

    @Test
    fun `an action not allowed in the current status is a 409 problem`() {
        val id = deliveries.create()

        action(id, "pickup")
            .expectStatus()
            .isEqualTo(409)
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("delivery.invalid-transition")
            .jsonPath("$.detail")
            .value<String> { assertThat(it).contains("CREATED") }

        assertThat(state.statusOf(id)).isEqualTo("CREATED")
    }

    @Test
    fun `an action on an unknown delivery is a 404`() {
        action(UUID.randomUUID(), "confirm")
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("delivery.not-found")
    }

    private fun confirmed() = deliveries.create().also { deliveries.changeStatus(it, DeliveryStatusJpa.CONFIRMED) }

    private fun action(
        id: UUID,
        name: String,
    ) = restTestClient
        .post()
        .uri("/api/deliveries/$id/$name")
        .exchange()
}

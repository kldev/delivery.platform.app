package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.BaseRestIntegrationTest
import com.pl.platform.delivery.infrastructure.persistence.DeliveryStatusJpa
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import com.pl.platform.delivery.testing.DriverDatabaseFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StatusDurationsEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @Autowired
    lateinit var drivers: DriverDatabaseFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
        deliveries.create()
        deliveries.create()
    }

    @Test
    fun `lists status durations of all deliveries`() {
        restTestClient
            .get()
            .uri("/api/deliveries/status-durations")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(2)
    }

    @Test
    fun `lists the history of one delivery, oldest first`() {
        val id = deliveries.create()
        deliveries.changeStatus(id, DeliveryStatusJpa.ASSIGNED, driverId = drivers.create())

        restTestClient
            .get()
            .uri("/api/deliveries/status-durations?deliveryId=$id")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(2)
            .jsonPath("$.content[0].status")
            .isEqualTo("CREATED")
            .jsonPath("$.content[0].statusTo")
            .exists()
            .jsonPath("$.content[1].status")
            .isEqualTo("ASSIGNED")
            .jsonPath("$.content[1].previousStatus")
            .isEqualTo("CREATED")
    }

    @Test
    fun `pages with size and page`() {
        restTestClient
            .get()
            .uri("/api/deliveries/status-durations?size=1&page=1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)
            .jsonPath("$.hasNext")
            .isEqualTo(false)
    }

    @Test
    fun `page size above the limit is a validation error`() {
        restTestClient
            .get()
            .uri("/api/deliveries/status-durations?size=600")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("size")
    }
}

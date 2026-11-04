package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.BaseRestIntegrationTest
import com.pl.platform.delivery.testing.DeliveryDatabaseFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.util.UUID

class GetDeliveriesEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var deliveries: DeliveryDatabaseFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `lists deliveries newest first`() {
        val first = deliveries.create()
        val second = deliveries.create()

        restTestClient
            .get()
            .uri("/api/deliveries")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(2)
            .jsonPath("$.content[0].id")
            .isEqualTo(second.toString())
            .jsonPath("$.content[1].id")
            .isEqualTo(first.toString())
            .jsonPath("$.hasNext")
            .isEqualTo(false)
    }

    @Test
    fun `returns an empty slice when there are no deliveries`() {
        restTestClient
            .get()
            .uri("/api/deliveries")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(0)
    }

    @Test
    fun `gets one delivery`() {
        val id = deliveries.create()

        restTestClient
            .get()
            .uri("/api/deliveries/$id")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id.toString())
            .jsonPath("$.status")
            .isEqualTo("CREATED")
    }

    @Test
    fun `an unknown delivery is a 404 problem`() {
        restTestClient
            .get()
            .uri("/api/deliveries/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("delivery.not-found")
    }

    @Test
    fun `page size above the limit is a validation error`() {
        restTestClient
            .get()
            .uri("/api/deliveries?size=600")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("validation")
            .jsonPath("$.errors[0].field")
            .isEqualTo("size")
    }

    @Test
    fun `an unknown status is a validation error`() {
        restTestClient
            .get()
            .uri("/api/deliveries?status=LOST")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("status")
    }
}

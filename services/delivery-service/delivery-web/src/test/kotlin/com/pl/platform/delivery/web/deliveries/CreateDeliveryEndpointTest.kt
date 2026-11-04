package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.BaseRestIntegrationTest
import com.pl.platform.delivery.infrastructure.messaging.outbox.InMemoryEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.util.UUID
import java.util.concurrent.TimeUnit

class CreateDeliveryEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var eventPublisher: InMemoryEventPublisher

    @BeforeEach
    fun clean() {
        eventPublisher.clear()
    }

    @Test
    fun `creates a delivery and points Location at it`() {
        val result =
            post(UUID.randomUUID(), body())
                .expectStatus()
                .isCreated
                .expectHeader()
                .exists("Location")
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("CREATED")
                .jsonPath("$.price")
                .isEqualTo(20.00)
                .jsonPath("$.currency")
                .isEqualTo("PLN")
                .returnResult()

        val id = jsonMapper.readTree(result.responseBodyContent).get("id").asString()
        assertThat(result.responseHeaders.location.toString()).isEqualTo("/api/deliveries/$id")

        restTestClient
            .get()
            .uri("/api/deliveries/$id")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.pickupAddress")
            .isEqualTo("Opole, Krakowska 10")
    }

    @Test
    fun `publishes delivery created`() {
        post(UUID.randomUUID(), body()).expectStatus().isCreated

        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            assertThat(eventPublisher.events()).anyMatch { it.eventType == "delivery.created" }
        }
    }

    @Test
    fun `the same key and body replay the first response without a second delivery`() {
        val key = UUID.randomUUID()
        val first = post(key, body()).expectStatus().isCreated.expectBody().returnResult()
        val before = countDeliveries()

        val second =
            post(key, body())
                .expectStatus()
                .isCreated
                .expectHeader()
                .valueEquals("Idempotent-Replayed", "true")
                .expectBody()
                .returnResult()

        assertThat(jsonMapper.readTree(second.responseBodyContent)).isEqualTo(jsonMapper.readTree(first.responseBodyContent))
        assertThat(second.responseHeaders.location).isEqualTo(first.responseHeaders.location)
        assertThat(countDeliveries()).isEqualTo(before)
    }

    @Test
    fun `the same key with another body is rejected with 422`() {
        val key = UUID.randomUUID()
        post(key, body()).expectStatus().isCreated

        post(key, body(pickupAddress = "Brzeg, Długa 10"))
            .expectStatus()
            .isEqualTo(422)
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("idempotency.key-reused")
    }

    @Test
    fun `a missing Idempotency-Key is a 400 with its own code`() {
        restTestClient
            .post()
            .uri("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("idempotency.key-missing")
    }

    @Test
    fun `an Idempotency-Key that is not a UUID is a 400`() {
        restTestClient
            .post()
            .uri("/api/deliveries")
            .header("Idempotency-Key", "not-a-uuid")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("idempotency.key-invalid")
    }

    @Test
    fun `invalid fields are listed by name`() {
        post(UUID.randomUUID(), """{"pickupAddress":"","deliveryAddress":"Wrocław","distanceKm":-1,"currency":"pln"}""")
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("validation")
            .jsonPath("$.errors[*].field")
            .value<List<String>> { assertThat(it).containsExactlyInAnyOrder("pickupAddress", "distanceKm", "currency") }
    }

    @Test
    fun `a missing required field is reported by name, not as unreadable JSON`() {
        post(UUID.randomUUID(), """{"pickupAddress":"Opole","deliveryAddress":"Wrocław"}""")
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("distanceKm")
    }

    private fun post(
        key: UUID,
        json: String,
    ) = restTestClient
        .post()
        .uri("/api/deliveries")
        .header("Idempotency-Key", key.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .body(json)
        .exchange()

    private fun body(pickupAddress: String = "Opole, Krakowska 10") =
        """{"pickupAddress":"$pickupAddress","deliveryAddress":"Wrocław, Rynek 1","distanceKm":100.00,"currency":"PLN"}"""

    private fun countDeliveries() = jdbcTemplate.queryForObject("SELECT count(*) FROM deliveries", Long::class.java)
}

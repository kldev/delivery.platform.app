package com.pl.platform.reconciliation.web.reconciliations

import com.pl.platform.reconciliation.BaseRestIntegrationTest
import com.pl.platform.reconciliation.testing.ReconciliationEvents
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.util.UUID

class ReconciliationEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var events: ReconciliationEvents

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `lists discrepancies only`() {
        val matching = UUID.randomUUID()
        val differing = UUID.randomUUID()
        events.settlement(matching, "7.00")
        events.payment(matching, "7.00")
        events.settlement(differing, "7.00")
        events.payment(differing, "9.00")

        restTestClient
            .get()
            .uri("/api/reconciliations?status=DISCREPANCY")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)
            .jsonPath("$.content[0].deliveryId")
            .isEqualTo(differing.toString())
            .jsonPath("$.content[0].difference")
            .isEqualTo(2.0)
    }

    @Test
    fun `finds by the exact external transaction id and gets one by id`() {
        val deliveryId = UUID.randomUUID()
        val paymentId = UUID.randomUUID()
        events.payment(deliveryId, "9.00", paymentId = paymentId)

        val id =
            jsonMapper
                .readTree(
                    restTestClient
                        .get()
                        .uri("/api/reconciliations?externalTransactionId=TX-$paymentId")
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .jsonPath("$.content.length()")
                        .isEqualTo(1)
                        .returnResult()
                        .responseBodyContent,
                ).get("content")
                .get(0)
                .get("id")
                .asString()

        restTestClient
            .get()
            .uri("/api/reconciliations?externalTransactionId=TX-")
            .exchange()
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(0)

        restTestClient
            .get()
            .uri("/api/reconciliations/$id")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("PENDING")
            .jsonPath("$.expectedAmount")
            .doesNotExist()
    }

    @Test
    fun `an unknown reconciliation is a 404 problem`() {
        restTestClient
            .get()
            .uri("/api/reconciliations/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("reconciliation.not-found")
    }

    @Test
    fun `invalid parameters are 400 problems with the field`() {
        for ((name, value) in listOf("size" to "600", "page" to "-1", "status" to "LOST")) {
            restTestClient
                .get()
                .uri("/api/reconciliations?$name=$value")
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody()
                .jsonPath("$.errors[0].field")
                .isEqualTo(name)
        }
    }
}

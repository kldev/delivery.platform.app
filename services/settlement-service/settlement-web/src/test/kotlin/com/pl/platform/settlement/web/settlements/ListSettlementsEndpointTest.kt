package com.pl.platform.settlement.web.settlements

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.settlement.BaseRestIntegrationTest
import com.pl.platform.settlement.domain.Settlement
import com.pl.platform.settlement.infrastructure.persistence.SpringDataSettlementRepository
import com.pl.platform.settlement.testing.SettlementDatabaseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.client.expectBody
import java.util.UUID
import kotlin.test.assertNotNull

class ListSettlementsEndpointTest : BaseRestIntegrationTest() {

    @Autowired
    private lateinit var settlementRepository: SpringDataSettlementRepository

    @Autowired
    private lateinit var settlementDatabaseFixture: SettlementDatabaseFixture

    @BeforeEach
    fun cleanUp() {
        settlementRepository.deleteAll()
    }

    @Test
    fun `should return settlements`() {

        settlementDatabaseFixture.create(
            driverFullName = "John Connor",
        )

        settlementDatabaseFixture.create(
            driverFullName = "Sarah Connor",
        )

        val response =
            restTestClient
                .get()
                .uri(url("/api/settlements"))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(2)

        assertThat(response.hasNext)
            .isFalse()
    }

    @Test
    fun `should search settlements by driver name`() {

        settlementDatabaseFixture.create(
            driverFullName = "John Connor",
        )

        settlementDatabaseFixture.create(
            driverFullName = "Sarah Connor",
        )

        val response =
            restTestClient
                .get()
                .uri(
                    url("/api/settlements?search=John")
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(1)

        assertThat(response.content[0].driverFullName)
            .isEqualTo("John Connor")
    }

    @Test
    fun `should search settlements case insensitive`() {

        settlementDatabaseFixture.create(
            driverFullName = "John Connor",
        )

        val response =
            restTestClient
                .get()
                .uri(
                    url("/api/settlements?search=jOhN")
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(1)

        assertThat(response.content[0].driverFullName)
            .isEqualTo("John Connor")
    }

    @Test
    fun `should filter settlements by driver id`() {

        val driverId = UUID.randomUUID()

        settlementDatabaseFixture.create(
            driverId = driverId,
            driverFullName = "John Connor",
        )

        settlementDatabaseFixture.create(
            driverId = UUID.randomUUID(),
            driverFullName = "Sarah Connor",
        )

        val response =
            restTestClient
                .get()
                .uri(
                    url("/api/settlements?driverId=$driverId")
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(1)

        assertThat(response.content[0].driverId)
            .isEqualTo(driverId)
    }

    @Test
    fun `should filter settlements by delivery id`() {

        val deliveryId = UUID.randomUUID()

        settlementDatabaseFixture.create(
            deliveryId = deliveryId,
            driverFullName = "John Connor",
        )

        settlementDatabaseFixture.create(
            deliveryId = UUID.randomUUID(),
            driverFullName = "Sarah Connor",
        )

        val response =
            restTestClient
                .get()
                .uri(
                    url("/api/settlements?deliveryId=$deliveryId")
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(1)

        assertThat(response.content[0].deliveryId)
            .isEqualTo(deliveryId)
    }

    @Test
    fun `should combine settlement filters`() {

        val driverId = UUID.randomUUID()

        settlementDatabaseFixture.create(
            driverId = driverId,
            driverFullName = "John Connor",
        )

        settlementDatabaseFixture.create(
            driverId = driverId,
            driverFullName = "Sarah Connor",
        )

        settlementDatabaseFixture.create(
            driverId = UUID.randomUUID(),
            driverFullName = "John Smith",
        )

        val response =
            restTestClient
                .get()
                .uri(
                    url(
                        "/api/settlements" +
                                "?search=John" +
                                "&driverId=$driverId"
                    )
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(1)

        assertThat(response.content[0].driverFullName)
            .isEqualTo("John Connor")

        assertThat(response.content[0].driverId)
            .isEqualTo(driverId)
    }

    @Test
    fun `should return empty result when settlement does not exist`() {

        val response =
            restTestClient
                .get()
                .uri(url("/api/settlements?search=Unknown"))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .isEmpty()

        assertThat(response.hasNext)
            .isFalse()
    }

    @Test
    fun `should paginate settlements`() {

        repeat(3) {
            settlementDatabaseFixture.create(
                driverFullName = "Driver $it",
            )
        }

        val response =
            restTestClient
                .get()
                .uri(
                    url("/api/settlements?page=0&size=2")
                )
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<SliceResponse<Settlement>>()
                .returnResult()
                .responseBody

        assertNotNull(response)

        assertThat(response.content)
            .hasSize(2)

        assertThat(response.hasNext)
            .isTrue()
    }

    @Test
    fun `gets one settlement, unknown is a 404 problem`() {
        val id = settlementDatabaseFixture.create(driverFullName = "Grace Hopper")

        restTestClient
            .get()
            .uri(url("/api/settlements/$id"))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.driverFullName")
            .isEqualTo("Grace Hopper")

        restTestClient
            .get()
            .uri(url("/api/settlements/${java.util.UUID.randomUUID()}"))
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("settlement.not-found")
    }

    @Test
    fun `wildcards in search are literal characters`() {
        settlementDatabaseFixture.create(driverFullName = "John Connor")

        restTestClient
            .get()
            .uri(url("/api/settlements?search=%25"))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(0)
    }

    @Test
    fun `page size above the limit is a validation error`() {
        restTestClient
            .get()
            .uri(url("/api/settlements?size=600"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("size")
    }
}

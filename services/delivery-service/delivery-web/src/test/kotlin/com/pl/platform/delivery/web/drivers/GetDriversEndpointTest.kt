package com.pl.platform.delivery.web.drivers

import com.pl.platform.delivery.BaseRestIntegrationTest
import com.pl.platform.delivery.infrastructure.persistence.DriverJpaEntity
import com.pl.platform.delivery.infrastructure.persistence.SpringDataDriverRepository
import com.pl.platform.delivery.testing.DriverTestFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

class GetDriversEndpointTest : BaseRestIntegrationTest() {

    @Autowired
    lateinit var driverRepository: SpringDataDriverRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase()
    }

    @Test
    fun `should return drivers`() {
        val first = DriverTestFactory.create()
        val second = DriverTestFactory.create()

        driverRepository.save(DriverJpaEntity.create(first))
        driverRepository.save(DriverJpaEntity.create(second))

        restTestClient
            .get()
            .uri("/api/drivers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should return empty list when no drivers exist`() {
        restTestClient
            .get()
            .uri("/api/drivers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should return driver details`() {
        val driver = DriverTestFactory.create()

        driverRepository.save(DriverJpaEntity.create(driver))

        restTestClient
            .get()
            .uri("/api/drivers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[0].id")
            .isEqualTo(driver.id.value.toString())
            .jsonPath("$[0].firstName")
            .isEqualTo(driver.firstName)
            .jsonPath("$[0].lastName")
            .isEqualTo(driver.lastName)
            .jsonPath("$[0].phoneNumber")
            .isEqualTo(driver.phoneNumber)
    }

    @Test
    fun `gets one driver`() {
        val driver = DriverTestFactory.create()
        driverRepository.save(DriverJpaEntity.create(driver))

        restTestClient
            .get()
            .uri("/api/drivers/${driver.id.value}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.firstName")
            .isEqualTo(driver.firstName)
    }

    @Test
    fun `an unknown driver is a 404 problem`() {
        restTestClient
            .get()
            .uri("/api/drivers/${java.util.UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("driver.not-found")
    }
}

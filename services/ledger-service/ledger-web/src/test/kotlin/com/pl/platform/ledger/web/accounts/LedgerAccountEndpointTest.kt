package com.pl.platform.ledger.web.accounts

import com.pl.platform.ledger.BaseRestIntegrationTest
import com.pl.platform.ledger.testing.LedgerFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.math.BigDecimal
import java.util.UUID

class LedgerAccountEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var ledger: LedgerFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `lists accounts with their current balance`() {
        val platform = ledger.platformAccount()
        val driver = ledger.driverAccount()
        ledger.settlement(platform, driver, BigDecimal("40.00"))

        restTestClient
            .get()
            .uri("/api/ledger-accounts?ownerType=DRIVER")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)
            .jsonPath("$.content[0].id")
            .isEqualTo(driver.id.toString())
            .jsonPath("$.content[0].ownerType")
            .isEqualTo("DRIVER")
            .jsonPath("$.content[0].balance")
            .isEqualTo(40.0)
            .jsonPath("$.content[0].balanceUpdatedAt")
            .exists()
    }

    @Test
    fun `gets one account, a new account has a zero balance`() {
        val platform = ledger.platformAccount("EUR")

        restTestClient
            .get()
            .uri("/api/ledger-accounts/${platform.id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.currency")
            .isEqualTo("EUR")
            .jsonPath("$.ownerId")
            .doesNotExist()
            .jsonPath("$.balance")
            .isEqualTo(0)
    }

    @Test
    fun `an unknown account is a 404 problem`() {
        restTestClient
            .get()
            .uri("/api/ledger-accounts/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.code")
            .isEqualTo("ledger-account.not-found")
    }

    @Test
    fun `an invalid currency filter is a validation error`() {
        restTestClient
            .get()
            .uri("/api/ledger-accounts?currency=pln")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("currency")
    }
}

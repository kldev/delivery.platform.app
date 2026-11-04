package com.pl.platform.ledger.web.entries

import com.pl.platform.ledger.BaseRestIntegrationTest
import com.pl.platform.ledger.testing.LedgerFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class LedgerEntryEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var ledger: LedgerFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `lists the entries of an account with their transaction`() {
        val platform = ledger.platformAccount()
        val driver = ledger.driverAccount()
        val transaction = ledger.settlement(platform, driver)

        restTestClient
            .get()
            .uri("/api/ledger-entries?accountId=${driver.id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)
            .jsonPath("$.content[0].type")
            .isEqualTo("CREDIT")
            .jsonPath("$.content[0].transactionId")
            .isEqualTo(transaction.id.toString())
    }

    @Test
    fun `filters by transaction`() {
        val platform = ledger.platformAccount()
        val transaction = ledger.settlement(platform, ledger.driverAccount())
        ledger.settlement(platform, ledger.driverAccount())

        restTestClient
            .get()
            .uri("/api/ledger-entries?transactionId=${transaction.id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(2)
    }

    @Test
    fun `page size above the limit is a validation error`() {
        restTestClient
            .get()
            .uri("/api/ledger-entries?size=600")
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
    fun `from after to is a validation error`() {
        restTestClient
            .get()
            .uri("/api/ledger-entries?from=2026-08-24&to=2026-08-23")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.errors[0].field")
            .isEqualTo("from")
    }
}

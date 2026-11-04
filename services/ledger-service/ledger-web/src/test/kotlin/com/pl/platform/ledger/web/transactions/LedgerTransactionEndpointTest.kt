package com.pl.platform.ledger.web.transactions

import com.pl.platform.ledger.BaseRestIntegrationTest
import com.pl.platform.ledger.testing.LedgerFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class LedgerTransactionEndpointTest : BaseRestIntegrationTest() {
    @Autowired
    lateinit var ledger: LedgerFixture

    @BeforeEach
    fun clean() {
        cleanDatabase()
    }

    @Test
    fun `lists transactions newest first, each with its entries`() {
        val platform = ledger.platformAccount()
        val older = ledger.settlement(platform, ledger.driverAccount(), occurredAt = Instant.parse("2026-08-22T10:00:00Z"))
        val newer = ledger.settlement(platform, ledger.driverAccount(), occurredAt = Instant.parse("2026-08-23T10:00:00Z"))

        restTestClient
            .get()
            .uri("/api/ledger-transactions")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(2)
            .jsonPath("$.content[0].id")
            .isEqualTo(newer.id.toString())
            .jsonPath("$.content[1].id")
            .isEqualTo(older.id.toString())
            .jsonPath("$.content[0].type")
            .isEqualTo("DRIVER_SETTLEMENT")
            .jsonPath("$.content[0].entries.length()")
            .isEqualTo(2)
    }

    @Test
    fun `filters by reference and by day`() {
        val platform = ledger.platformAccount()
        val settlementId = UUID.randomUUID()
        ledger.settlement(
            platform,
            ledger.driverAccount(),
            BigDecimal("15.00"),
            Instant.parse("2026-08-23T21:30:00Z"), // 23:30 in Warsaw
            settlementId,
        )
        ledger.settlement(platform, ledger.driverAccount(), occurredAt = Instant.parse("2026-08-25T10:00:00Z"))

        restTestClient
            .get()
            .uri("/api/ledger-transactions?referenceType=SETTLEMENT&referenceId=$settlementId")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)

        restTestClient
            .get()
            .uri("/api/ledger-transactions?from=2026-08-23&to=2026-08-23")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.content.length()")
            .isEqualTo(1)
            .jsonPath("$.content[0].referenceId")
            .isEqualTo(settlementId.toString())
    }

    @Test
    fun `the old misspelled path is gone`() {
        restTestClient.get().uri("/api/ledger-transations").exchange().expectStatus().isNotFound
    }
}

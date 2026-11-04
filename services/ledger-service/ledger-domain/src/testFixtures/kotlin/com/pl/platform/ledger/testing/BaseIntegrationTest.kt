package com.pl.platform.ledger.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

/**
 * Spring context on a real PostgreSQL with the production Flyway migrations.
 * In ledger-domain the context comes from LedgerDomainTestApplication, in ledger-web from the real application.
 */
@SpringBootTest
@Import(PostgresContainerConfiguration::class)
@ActiveProfiles("test")
abstract class BaseIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    /** Empties the ledger, including the platform accounts seeded by V4; tests create what they need. */
    protected fun cleanDatabase() {
        jdbcTemplate.execute(
            """
            TRUNCATE TABLE
                ledger_entries,
                ledger_transactions,
                ledger_account_balances,
                ledger_accounts,
                processed_events
            CASCADE
            """.trimIndent(),
        )
    }
}

package com.pl.platform.reconciliation.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

/**
 * Spring context on a real PostgreSQL with the production Flyway migrations. In reconciliation-domain the
 * context comes from ReconciliationDomainTestApplication, in reconciliation-web from the real application.
 */
@SpringBootTest
@Import(PostgresContainerConfiguration::class)
@ActiveProfiles("test")
abstract class BaseIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    protected fun cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE reconciliations, processed_events")
    }
}

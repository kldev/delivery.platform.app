package com.pl.platform.delivery.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

/**
 * Spring context on a real PostgreSQL with the production Flyway migrations.
 * In delivery-domain the context comes from DeliveryDomainTestApplication, in delivery-web from the real application.
 */
@SpringBootTest
@Import(PostgresContainerConfiguration::class)
@ActiveProfiles("test")
abstract class BaseIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    protected fun cleanDatabase() {
        jdbcTemplate.execute(
            """
            TRUNCATE TABLE
                drivers,
                deliveries
            CASCADE
            """.trimIndent(),
        )
    }
}

package com.pl.platform.notification.testing

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.postgresql.PostgreSQLContainer

/** PostgreSQL in the same major as docker-compose.yml; Flyway runs the real migrations at start. */
class PostgresTestResource : QuarkusTestResourceLifecycleManager {
    private val postgres = PostgreSQLContainer("postgres:17")

    override fun start(): Map<String, String> {
        postgres.start()
        return mapOf(
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "quarkus.datasource.reactive.url" to postgres.jdbcUrl.replace("jdbc:postgresql://", "postgresql://"),
            "quarkus.datasource.jdbc.url" to postgres.jdbcUrl,
        )
    }

    override fun stop() {
        postgres.stop()
    }
}

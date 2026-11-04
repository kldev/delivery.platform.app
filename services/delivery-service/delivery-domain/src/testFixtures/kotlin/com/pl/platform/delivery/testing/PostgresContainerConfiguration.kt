package com.pl.platform.delivery.testing

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class PostgresContainerConfiguration {
    // Same major as docker-compose.yml. @ServiceConnection fills spring.datasource.*; Flyway runs the real migrations.
    @Bean
    @ServiceConnection
    fun postgres() = PostgreSQLContainer("postgres:17")
}

package com.pl.platform.delivery.application.shared.idempotency

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * @property ttl how long a key replays its first response (IDEMPOTENCY_TTL)
 * @property lockTimeout how long a request waits for a concurrent one with the same key before 409 (IDEMPOTENCY_LOCK_TIMEOUT)
 */
@ConfigurationProperties("idempotency")
data class IdempotencyProperties(
    val ttl: Duration = Duration.ofHours(24),
    val lockTimeout: Duration = Duration.ofSeconds(5),
)

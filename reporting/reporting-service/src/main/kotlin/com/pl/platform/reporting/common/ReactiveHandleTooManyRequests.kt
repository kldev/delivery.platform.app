package com.pl.platform.reporting.common
import com.pl.platform.svc.common.RetryExhaustedException
import com.pl.platform.svc.common.TooManyRequestsException
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import java.time.Duration
import java.util.UUID
import java.util.function.Supplier
import kotlin.random.Random

object ReactiveHandleTooManyRequests {

    private const val MAX_RETRIES = 3
    private const val INITIAL_BACKOFF_MS = 500L
    private const val MAX_BACKOFF_MS = 5_000L

    fun <T> handleTooManyRequestsExceptionUni(
        exception: TooManyRequestsException,
        retryCount: Int,
        action: () -> Uni<T>,
        identifier: UUID,
        operation: String
    ): Uni<T> {

        if (retryCount >= MAX_RETRIES) {
            return Uni.createFrom().failure(
                RetryExhaustedException(
                    operation = operation,
                    attempts = retryCount,
                    cause = exception
                )
            )
        }

        val delay = calculateRetryDelay(
            retryCount,
            exception.retryAfterSeconds
        )

        Log.debugf(
            "Remote API returned 429 for %s, retrying in %d ms, attempt=%d/%d, id=%s",
            operation,
            delay.toMillis(),
            retryCount + 1,
            MAX_RETRIES,
            identifier
        )

        return Uni.createFrom()
            .item(Unit)
            .onItem()
            .delayIt()
            .by(delay)
            .chain(Supplier {
                action()
            })
    }

    private fun calculateRetryDelay(
        attempt: Int,
        retryAfterSeconds: Long?
    ): Duration {

        if (retryAfterSeconds != null) {
            return Duration.ofSeconds(
                retryAfterSeconds.coerceAtLeast(1)
            )
        }

        val exponentialDelay = (
                INITIAL_BACKOFF_MS * (1L shl attempt)
                ).coerceAtMost(MAX_BACKOFF_MS)

        val jitter = Random.nextLong(
            from = 0,
            until = (exponentialDelay / 2).coerceAtLeast(1)
        )

        return Duration.ofMillis(
            exponentialDelay + jitter
        )
    }
}
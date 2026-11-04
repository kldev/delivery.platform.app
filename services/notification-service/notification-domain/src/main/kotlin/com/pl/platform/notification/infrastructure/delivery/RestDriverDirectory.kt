package com.pl.platform.notification.infrastructure.delivery

import com.github.benmanes.caffeine.cache.Caffeine
import com.pl.platform.notification.application.port.DriverDirectory
import com.pl.platform.notification.domain.NotificationRecipient
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

/** Driver of a delivery from delivery-service, with retries on 429 and a short cache of drivers. */
@ApplicationScoped
class RestDriverDirectory(
    @RestClient private val deliveryClient: DeliveryClient,
) : DriverDirectory {
    private val drivers =
        Caffeine
            .newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build<UUID, NotificationRecipient>()

    override fun driverOfDelivery(deliveryId: UUID): Uni<NotificationRecipient?> =
        withRetry("get delivery $deliveryId") { deliveryClient.getDelivery(deliveryId) }
            .map<DeliveryResponse?> { it }
            .onFailure(RemoteNotFoundException::class.java)
            .recoverWithNull()
            .flatMap { delivery ->
                val driverId = delivery?.driverId ?: return@flatMap Uni.createFrom().nullItem<NotificationRecipient>()
                drivers.getIfPresent(driverId)?.let { return@flatMap Uni.createFrom().item(it) }
                withRetry("get driver $driverId") { deliveryClient.getDriver(driverId) }
                    .map<NotificationRecipient?> { driver ->
                        NotificationRecipient(
                            fullName = "${driver.firstName} ${driver.lastName}",
                            phoneNumber = driver.phoneNumber,
                            email = driver.email,
                        ).also { drivers.put(driverId, it) }
                    }.onFailure(RemoteNotFoundException::class.java)
                    .recoverWithNull()
            }

    private fun <T> withRetry(
        operation: String,
        attempt: Int = 0,
        call: () -> Uni<T>,
    ): Uni<T> =
        call()
            .onFailure(TooManyRequestsException::class.java)
            .recoverWithUni { failure ->
                if (attempt >= MAX_RETRIES) {
                    Log.warnf("Delivery API rate limit: %s failed after %d retries", operation, attempt)
                    return@recoverWithUni Uni.createFrom().failure(failure)
                }
                val delay = retryDelay(attempt, (failure as TooManyRequestsException).retryAfterSeconds)
                Log.debugf("Delivery API returned 429 for %s, retry %d in %d ms", operation, attempt + 1, delay.toMillis())
                Uni
                    .createFrom()
                    .item(Unit)
                    .onItem()
                    .delayIt()
                    .by(delay)
                    .flatMap { withRetry(operation, attempt + 1, call) }
            }

    private fun retryDelay(
        attempt: Int,
        retryAfterSeconds: Long?,
    ): Duration {
        if (retryAfterSeconds != null) return Duration.ofSeconds(retryAfterSeconds.coerceAtLeast(1))
        val exponential = (INITIAL_BACKOFF_MS * (1L shl attempt)).coerceAtMost(MAX_BACKOFF_MS)
        val jitter = Random.nextLong(0, (exponential / 2).coerceAtLeast(1))
        return Duration.ofMillis(exponential + jitter)
    }

    private companion object {
        const val MAX_RETRIES = 3
        const val INITIAL_BACKOFF_MS = 500L
        const val MAX_BACKOFF_MS = 5_000L
    }
}

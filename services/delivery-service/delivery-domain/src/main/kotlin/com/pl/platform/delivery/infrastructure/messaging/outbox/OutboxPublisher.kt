package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.port.EventPublisher
import com.pl.platform.common.messaging.port.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val BATCH_SIZE = 50
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_BACKOFF_SECONDS = 5L
        private const val MAX_BACKOFF_SECONDS = 300L
    }

    @Scheduled(fixedDelayString = "\${delivery.outbox.publish-interval-ms:1000}")
    fun publishPending() {

        val events = outboxRepository.findPending(
            limit = BATCH_SIZE,
            now = Instant.now()
        )

        events.forEach { event ->

            try {
                eventPublisher.publish(event)

                outboxRepository.markPublished(
                    id = event.id,
                    publishedAt = Instant.now()
                )

            } catch (exception: Exception) {
                val attempts = event.attempts + 1

                if (attempts >= MAX_ATTEMPTS) {
                    log.info("Mark event as dead {} ", event.id.value)
                    outboxRepository.markDead(
                        id = event.id,
                        error = exception.message ?: exception.javaClass.simpleName
                    )

                    return@forEach
                }

                val nextAttemptAt =
                    calculateNextAttempt(
                        attempts = attempts
                    )

                log.warn(
                    "Failed to publish outbox event id={} type={} attempt={}",
                    event.id.value,
                    event.eventType,
                    attempts,
                    exception
                )

                outboxRepository.markFailed(
                    id = event.id,
                    nextAttemptAt = nextAttemptAt,
                    error = exception.message ?: exception.javaClass.simpleName
                )
            }
        }
    }

    private fun calculateNextAttempt(attempts: Int): Instant {

        val multiplier =
            1L shl minOf(attempts, 6)

        val delaySeconds =
            minOf(
                INITIAL_BACKOFF_SECONDS * multiplier,
                MAX_BACKOFF_SECONDS
            )

        return Instant.now().plus(
            Duration.ofSeconds(delaySeconds)
        )
    }
}
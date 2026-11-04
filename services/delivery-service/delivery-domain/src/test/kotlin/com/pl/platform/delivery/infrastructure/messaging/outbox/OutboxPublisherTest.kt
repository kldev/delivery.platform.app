package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxMessage
import com.pl.platform.common.messaging.OutboxMessageId
import com.pl.platform.common.messaging.OutboxStatus
import com.pl.platform.common.messaging.port.EventPublisher
import com.pl.platform.common.messaging.port.OutboxRepository
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class OutboxPublisherTest {

    private val outboxRepository =
        mockk<OutboxRepository>()

    private val eventPublisher =
        mockk<EventPublisher>()

    private val publisher =
        OutboxPublisher(
            outboxRepository,
            eventPublisher
        )

    @Test
    fun `should mark event as published`() {

        val id = OutboxMessageId(UUID.randomUUID())

        val event =
            OutboxMessage(
                id = id,
                module = "delivery",
                eventId = UUID.randomUUID(),
                aggregateId = UUID.randomUUID(),
                eventType = "delivery.created",
                payload = "{}",
                status = OutboxStatus.PENDING,
                attempts = 0,
                nextAttemptAt = Instant.now(),
                occurredAt = Instant.now(),
                createdAt = Instant.now(),
                publishedAt = null,
                lastError = null,
                lockedUntil = null
            )

        every {
            outboxRepository.findPending(any(), any())
        } returns listOf(event)

        every {
            eventPublisher.publish(event)
        } just Runs

        every {
            outboxRepository.markPublished(any(), any())
        } just Runs

        publisher.publishPending()

        verify {
            eventPublisher.publish(event)
        }

        verify {
            outboxRepository.markPublished(
                id,
                any()
            )
        }
    }
}
package com.pl.platform.delivery.infrastructure.messaging.outbox

import com.pl.platform.common.messaging.OutboxMessage
import com.pl.platform.common.messaging.OutboxMessageId
import com.pl.platform.common.messaging.OutboxStatus
import com.pl.platform.common.messaging.event.Event
import com.pl.platform.common.messaging.port.OutboxRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.*

@Component
class JpaOutboxRepository(
    private val repository: SpringDataOutboxRepository,
    private val jsonMapper: JsonMapper
) : OutboxRepository {

    override fun save(event: Event) {
        val now = Instant.now()

        val entity = OutboxMessageJpaEntity(
            id = UUID.randomUUID(),
            eventId =    event.eventId,
            aggregateId = event.aggregateId,
            module = event.module,
            eventType = event.eventType,
            payload = jsonMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            attempts = 0,
            nextAttemptAt = now,
            occurredAt = event.occurredAt,
            createdAt = now,
            publishedAt = null,
            lastError = null,
            lockedUntil = null
        )

        repository.save(entity)
    }

    @Transactional
    override fun findPending(
        limit: Int,
        now: Instant
    ): List<OutboxMessage> {
        return repository.claimPending(
            now = now,
            limit = limit,
            lockedUntil = now.plusSeconds(60)
        ).map { it.toDomain() }
    }

    @Transactional
    override fun markPublished(
        id: OutboxMessageId,
        publishedAt: Instant
    ) {
        repository.markPublished(
            id = id.value,
            publishedAt = publishedAt
        )
    }

    @Transactional
    override fun markFailed(
        id: OutboxMessageId,
        nextAttemptAt: Instant,
        error: String
    ) {
        repository.markFailed(
            id = id.value,
            nextAttemptAt = nextAttemptAt,
            error = error.take(4000)
        )
    }

    @Transactional
    override fun markDead(id: OutboxMessageId, error: String) {
        repository.markDead(id = id.value, error = error)
    }

    private fun OutboxMessageJpaEntity.toDomain() =
        OutboxMessage(
            id = OutboxMessageId(id),
            aggregateId = aggregateId,
            eventType = eventType,
            module = module,
            payload = payload,
            status = status,
            attempts = attempts,
            nextAttemptAt = nextAttemptAt,
            occurredAt = occurredAt,
            createdAt = createdAt,
            publishedAt = publishedAt,
            lastError = lastError,
            lockedUntil = lockedUntil,
            eventId = eventId
        )
}
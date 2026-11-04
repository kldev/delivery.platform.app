package com.pl.platform.common.messaging

import java.time.Instant
import java.util.UUID

@JvmInline
value class OutboxMessageId(
    val value: UUID
)

enum class OutboxStatus {
    PENDING,
    PUBLISHED,
    DEAD
}

data class OutboxMessage(
    val id: OutboxMessageId,
    val module: String,
    var aggregateId: UUID,
    val eventId: UUID,
    val eventType: String, // TOPIC
    val payload: String,
    val status: OutboxStatus,
    val attempts: Int,
    val nextAttemptAt: Instant,
    val occurredAt: Instant,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val lastError: String?,
    val lockedUntil: Instant?
)
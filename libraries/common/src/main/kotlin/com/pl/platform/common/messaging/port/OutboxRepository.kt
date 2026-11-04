package com.pl.platform.common.messaging.port

import com.pl.platform.common.messaging.OutboxMessage
import com.pl.platform.common.messaging.OutboxMessageId
import com.pl.platform.common.messaging.event.Event
import java.time.Instant

interface OutboxRepository {

    fun save(event: Event )

    fun findPending(
        limit: Int,
        now: Instant
    ): List<OutboxMessage>

    fun markPublished(
        id: OutboxMessageId,
        publishedAt: Instant
    )

    fun markFailed(
        id: OutboxMessageId,
        nextAttemptAt: Instant,
        error: String
    )

    fun markDead(id: OutboxMessageId,   error: String)
}
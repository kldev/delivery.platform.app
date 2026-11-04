package com.pl.platform.common.messaging.event

import java.time.Instant
import java.util.UUID

data class EventMetadata(
    override val eventId: UUID,
    override val occurredAt: Instant,
    override val aggregateId: UUID,
    override val module: String,
    override val eventType: String
) : Event {
}
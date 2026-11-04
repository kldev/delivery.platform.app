package com.pl.platform.common.messaging.event

import java.time.Instant
import java.util.UUID

data class TestEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: UUID = UUID.randomUUID(),
    override val module: String,
    override val eventType: String = "test.event"
) : Event {
}
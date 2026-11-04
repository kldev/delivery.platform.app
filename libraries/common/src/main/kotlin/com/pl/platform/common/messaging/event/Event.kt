package com.pl.platform.common.messaging.event

import java.time.Instant
import java.util.UUID

interface Event {
    val eventId: UUID
    val occurredAt: Instant
    val aggregateId: UUID
    val module: String
    val eventType: String
}
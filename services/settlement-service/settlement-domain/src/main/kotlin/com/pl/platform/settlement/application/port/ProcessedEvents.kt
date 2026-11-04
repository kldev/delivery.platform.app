package com.pl.platform.settlement.application.port

import java.util.UUID

/** Inbox of consumed integration events, for idempotent consumers. */
interface ProcessedEvents {
    fun isProcessed(eventId: UUID): Boolean

    fun markProcessed(
        eventId: UUID,
        eventType: String,
    )
}

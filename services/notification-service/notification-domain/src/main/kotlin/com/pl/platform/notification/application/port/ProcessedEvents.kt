package com.pl.platform.notification.application.port

import io.smallrye.mutiny.Uni
import java.util.UUID

/** Inbox of consumed integration events, for idempotent consumers. */
interface ProcessedEvents {
    /** Records the event in [transaction]; false when it had already been processed. */
    fun markProcessed(
        transaction: Transaction,
        eventId: UUID,
        eventType: String,
    ): Uni<Boolean>
}

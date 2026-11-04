package com.pl.platform.notification.domain

import java.time.Instant
import java.util.UUID

/**
 * A message to send over one channel because of one event. The send itself is a state machine kept in SQL
 * (PENDING -> PROCESSING -> SENT, or back to PENDING until [MAX_ATTEMPTS], then FAILED).
 * [recipient] is null until it is resolved from the delivery (or given by the creator).
 */
data class Notification(
    val id: NotificationId,
    val eventId: UUID,
    val eventType: String,
    val recipient: NotificationRecipient?,
    val channel: NotificationChannel,
    val payload: String,
    val status: NotificationStatus,
    val attempts: Int,
    val lastError: String? = null,
    val createdAt: Instant,
    val sentAt: Instant? = null,
) {
    companion object {
        /** A failed send goes back to PENDING until this many attempts, then FAILED. */
        const val MAX_ATTEMPTS = 5

        fun pending(
            eventId: UUID,
            eventType: String,
            channel: NotificationChannel,
            payload: String,
            recipient: NotificationRecipient? = null,
            createdAt: Instant = Instant.now(),
        ) = Notification(
            id = NotificationId(UUID.randomUUID()),
            eventId = eventId,
            eventType = eventType,
            recipient = recipient,
            channel = channel,
            payload = payload,
            status = NotificationStatus.PENDING,
            attempts = 0,
            createdAt = createdAt,
        )
    }
}

package com.pl.platform.notification.infrastructure.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationId
import com.pl.platform.notification.domain.NotificationRecipient
import com.pl.platform.notification.domain.NotificationStatus
import io.vertx.mutiny.sqlclient.Row
import jakarta.enterprise.context.ApplicationScoped

/** Row <-> Notification for the notifications table; recipient and payload are JSONB. */
@ApplicationScoped
class NotificationRows(
    private val objectMapper: ObjectMapper,
) {
    fun toNotification(row: Row): Notification =
        Notification(
            id = NotificationId(row.getUUID("id")),
            eventId = row.getUUID("event_id"),
            eventType = row.getString("event_type"),
            recipient = row.getValue("recipient")?.let { objectMapper.readValue(it.toString(), NotificationRecipient::class.java) },
            channel = NotificationChannel.valueOf(row.getString("channel")),
            payload = row.getValue("payload").toString(),
            status = NotificationStatus.valueOf(row.getString("status")),
            attempts = row.getInteger("attempts"),
            lastError = row.getString("last_error"),
            createdAt = row.getOffsetDateTime("created_at").toInstant(),
            sentAt = row.getOffsetDateTime("sent_at")?.toInstant(),
        )

    fun recipientJson(recipient: NotificationRecipient?): String? = recipient?.let(objectMapper::writeValueAsString)

    companion object {
        const val COLUMNS =
            "id, event_id, event_type, recipient, channel, payload, status, attempts, last_error, created_at, sent_at"
    }
}

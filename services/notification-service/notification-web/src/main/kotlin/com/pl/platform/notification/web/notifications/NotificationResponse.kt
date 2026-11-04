package com.pl.platform.notification.web.notifications

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pl.platform.notification.domain.Notification
import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val eventId: UUID,
    val eventType: String,
    val channel: NotificationChannelDto,
    val recipient: RecipientDto?,
    val payload: JsonNode,
    val status: NotificationStatusDto,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Instant,
    val sentAt: Instant?,
) {
    companion object {
        fun from(
            notification: Notification,
            objectMapper: ObjectMapper,
        ) = NotificationResponse(
            id = notification.id.value,
            eventId = notification.eventId,
            eventType = notification.eventType,
            channel = NotificationChannelDto.from(notification.channel),
            recipient = notification.recipient?.let(RecipientDto::from),
            payload = objectMapper.readTree(notification.payload),
            status = NotificationStatusDto.from(notification.status),
            attempts = notification.attempts,
            lastError = notification.lastError,
            createdAt = notification.createdAt,
            sentAt = notification.sentAt,
        )
    }
}

package com.pl.platform.notification.testing

import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationId
import com.pl.platform.notification.domain.NotificationRecipient
import com.pl.platform.notification.domain.NotificationStatus
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.UUID

/** Inserts notifications through the repository port. */
@ApplicationScoped
class NotificationFixture(
    private val repository: NotificationRepository,
) {
    fun create(
        eventId: UUID = UUID.randomUUID(),
        eventType: String = "delivery.assigned",
        recipient: NotificationRecipient? = NotificationRecipient(fullName = "Ada Lovelace", email = "ada@example.com"),
        channel: NotificationChannel = NotificationChannel.EMAIL,
        payload: String = """{"deliveryId":"${UUID.randomUUID()}"}""",
        status: NotificationStatus = NotificationStatus.PENDING,
        attempts: Int = 0,
        createdAt: Instant = Instant.now(),
    ): Notification =
        repository
            .insert(
                Notification(
                    id = NotificationId(UUID.randomUUID()),
                    eventId = eventId,
                    eventType = eventType,
                    recipient = recipient,
                    channel = channel,
                    payload = payload,
                    status = status,
                    attempts = attempts,
                    createdAt = createdAt,
                ),
            ).await()
            .indefinitely()

    fun find(id: NotificationId): Notification = checkNotNull(repository.findById(id).await().indefinitely())
}

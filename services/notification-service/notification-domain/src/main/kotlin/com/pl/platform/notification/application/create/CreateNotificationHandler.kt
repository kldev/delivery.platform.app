package com.pl.platform.notification.application.create

import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.domain.Notification
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CreateNotificationHandler(
    private val notifications: NotificationRepository,
) {
    fun handle(command: CreateNotificationCommand): Uni<Notification> =
        notifications.insert(
            Notification.pending(
                eventId = command.eventId,
                eventType = command.eventType,
                channel = command.channel,
                payload = command.payload,
                recipient = command.recipient,
            ),
        )
}

package com.pl.platform.notification.infrastructure.sender

import com.pl.platform.notification.application.port.NotificationSender
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationRecipient
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

/** Stub: logs instead of sending. */
@ApplicationScoped
class PushNotificationSender : NotificationSender {
    override val channel = NotificationChannel.PUSH

    override fun send(
        notification: Notification,
        recipient: NotificationRecipient,
    ): Uni<Void> {
        val target =
            recipient.fullName?.takeIf { it.isNotBlank() }
                ?: return Uni.createFrom().failure(IllegalStateException("Recipient has no fullName for PUSH"))
        Log.infof("Sending %s notification %s to %s", channel, notification.id.value, target)
        return Uni.createFrom().voidItem()
    }
}

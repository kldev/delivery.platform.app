package com.pl.platform.notification.application.sendpending

import com.pl.platform.notification.application.port.NotificationQueue
import com.pl.platform.notification.application.port.NotificationSenders
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationRecipient
import io.quarkus.logging.Log
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

/** Sends a batch of queued notifications one by one; each ends SENT, back in PENDING, or FAILED. */
@ApplicationScoped
class SendPendingNotificationsHandler(
    private val queue: NotificationQueue,
    private val recipients: RecipientResolver,
    private val senders: NotificationSenders,
) {
    fun handle(command: SendPendingNotificationsCommand): Uni<Void> =
        queue
            .claim(command.batchSize, command.lease)
            .invoke { batch -> if (batch.isNotEmpty()) Log.infof("Sending %d notifications", batch.size) }
            .onItem()
            .transformToMulti { Multi.createFrom().iterable(it) }
            .onItem()
            .transformToUniAndConcatenate(::send)
            .collect()
            .asList()
            .replaceWithVoid()

    private fun send(notification: Notification): Uni<Void> =
        recipients
            .resolve(notification)
            .flatMap { recipient ->
                if (recipient == null) {
                    queue.markFailed(notification.id, "Recipient could not be resolved")
                } else {
                    send(notification, recipient)
                }
            }.onFailure()
            .recoverWithUni { error ->
                Log.errorf(error, "Failed to send notification %s", notification.id.value)
                queue.markFailed(notification.id, error.message ?: error.javaClass.simpleName)
            }

    private fun send(
        notification: Notification,
        recipient: NotificationRecipient,
    ): Uni<Void> {
        val sender =
            senders.forChannel(notification.channel)
                ?: return queue.markFailed(notification.id, "Unsupported notification channel: ${notification.channel}")
        return sender.send(notification, recipient).flatMap { queue.markSent(notification.id) }
    }
}

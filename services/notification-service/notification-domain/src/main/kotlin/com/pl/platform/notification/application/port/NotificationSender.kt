package com.pl.platform.notification.application.port

import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationRecipient
import io.smallrye.mutiny.Uni

interface NotificationSender {
    val channel: NotificationChannel

    /** Fails when the recipient lacks the field this channel needs; the queue then retries or gives up. */
    fun send(
        notification: Notification,
        recipient: NotificationRecipient,
    ): Uni<Void>
}

interface NotificationSenders {
    fun forChannel(channel: NotificationChannel): NotificationSender?
}

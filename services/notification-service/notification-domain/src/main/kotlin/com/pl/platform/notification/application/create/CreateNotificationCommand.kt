package com.pl.platform.notification.application.create

import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationRecipient
import java.util.UUID

/** [payload] is a JSON document; [recipient] null means: resolve it from the delivery in the payload. */
data class CreateNotificationCommand(
    val eventId: UUID,
    val eventType: String,
    val channel: NotificationChannel,
    val payload: String,
    val recipient: NotificationRecipient?,
)

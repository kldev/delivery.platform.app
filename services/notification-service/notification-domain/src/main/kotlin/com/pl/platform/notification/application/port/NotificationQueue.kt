package com.pl.platform.notification.application.port

import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationId
import com.pl.platform.notification.domain.NotificationRecipient
import io.smallrye.mutiny.Uni
import java.time.Duration

/** The send queue kept in the notifications table. */
interface NotificationQueue {
    /**
     * Takes up to [batchSize] PENDING notifications, plus PROCESSING ones claimed longer than [lease] ago (the
     * process that took them died), marks them PROCESSING and counts the attempt.
     */
    fun claim(
        batchSize: Int,
        lease: Duration,
    ): Uni<List<Notification>>

    fun saveRecipient(
        id: NotificationId,
        recipient: NotificationRecipient,
    ): Uni<Void>

    fun markSent(id: NotificationId): Uni<Void>

    /** Back to PENDING for another attempt, or FAILED once Notification.MAX_ATTEMPTS attempts were made. */
    fun markFailed(
        id: NotificationId,
        error: String,
    ): Uni<Void>
}

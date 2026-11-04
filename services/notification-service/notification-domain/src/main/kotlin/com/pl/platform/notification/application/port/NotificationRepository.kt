package com.pl.platform.notification.application.port

import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotificationId
import io.smallrye.mutiny.Uni

interface NotificationRepository {
    /** Fails with ConflictException `notification.duplicate` when the event already has one on this channel. */
    fun insert(notification: Notification): Uni<Notification>

    fun insert(
        transaction: Transaction,
        notification: Notification,
    ): Uni<Notification>

    fun findById(id: NotificationId): Uni<Notification?>
}

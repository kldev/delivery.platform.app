package com.pl.platform.notification.application.getnotification

import com.pl.platform.notification.application.port.NotificationRepository
import com.pl.platform.notification.domain.Notification
import com.pl.platform.notification.domain.NotFoundException
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GetNotificationHandler(
    private val notifications: NotificationRepository,
) {
    fun handle(query: GetNotificationQuery): Uni<Notification> =
        notifications
            .findById(query.id)
            .onItem()
            .ifNull()
            .failWith { NotFoundException("notification", query.id.value) }
            .map { checkNotNull(it) }
}

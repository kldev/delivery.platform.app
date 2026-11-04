package com.pl.platform.notification.application.listnotifications

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.notification.application.port.NotificationQueryRepository
import com.pl.platform.notification.domain.Notification
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ListNotificationsHandler(
    private val notifications: NotificationQueryRepository,
) {
    fun handle(query: ListNotificationsQuery): Uni<SliceResponse<Notification>> = notifications.find(query)
}

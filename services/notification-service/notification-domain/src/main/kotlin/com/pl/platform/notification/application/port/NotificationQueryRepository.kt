package com.pl.platform.notification.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.notification.application.listnotifications.ListNotificationsQuery
import com.pl.platform.notification.domain.Notification
import io.smallrye.mutiny.Uni

interface NotificationQueryRepository {
    /** Newest first. */
    fun find(query: ListNotificationsQuery): Uni<SliceResponse<Notification>>
}

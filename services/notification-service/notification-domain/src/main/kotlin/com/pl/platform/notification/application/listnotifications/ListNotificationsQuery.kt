package com.pl.platform.notification.application.listnotifications

import com.pl.platform.notification.application.port.PageQuery
import com.pl.platform.notification.domain.NotificationStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** [from] and [to] are calendar days (of createdAt) in [timezone], both inclusive. */
data class ListNotificationsQuery(
    val status: NotificationStatus? = null,
    val eventId: UUID? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val timezone: ZoneId = ZoneId.of("Europe/Warsaw"),
    val page: PageQuery = PageQuery(),
) {
    fun fromAtInstant(): Instant? = from?.atStartOfDay(timezone)?.toInstant()

    fun toAtInstant(): Instant? = to?.plusDays(1)?.atStartOfDay(timezone)?.toInstant()
}

package com.pl.platform.delivery.application.liststatusdurations

import com.pl.platform.delivery.application.port.PageQuery
import com.pl.platform.delivery.domain.DeliveryStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ListStatusDurationsQuery(
    val deliveryId: UUID? = null,
    val page: PageQuery = PageQuery(),
)

data class DeliveryStatusDuration(
    val id: Long,
    val deliveryId: UUID,
    val status: DeliveryStatus,
    val previousStatus: DeliveryStatus?,
    val statusFrom: Instant?,
    val statusTo: Instant?,
    val durationSeconds: BigDecimal,
)

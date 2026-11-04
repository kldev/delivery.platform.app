package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.liststatusdurations.DeliveryStatusDuration
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class DeliveryStatusDurationResponse(
    val id: Long,
    val deliveryId: UUID,
    val status: DeliveryStatusDto,
    val previousStatus: DeliveryStatusDto?,
    val statusFrom: Instant?,
    val statusTo: Instant?,
    val durationSeconds: BigDecimal,
) {
    companion object {
        fun from(item: DeliveryStatusDuration) =
            DeliveryStatusDurationResponse(
                id = item.id,
                deliveryId = item.deliveryId,
                status = DeliveryStatusDto.from(item.status),
                previousStatus = item.previousStatus?.let(DeliveryStatusDto::from),
                statusFrom = item.statusFrom,
                statusTo = item.statusTo,
                durationSeconds = item.durationSeconds,
            )
    }
}

package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.port.DeliveryState
import java.util.UUID

/** Result of a delivery action (confirm, assign-driver, pickup, start, complete, cancel). */
data class DeliveryStateResponse(
    val id: UUID,
    val status: DeliveryStatusDto,
    val driverId: UUID?,
) {
    companion object {
        fun from(state: DeliveryState) =
            DeliveryStateResponse(
                id = state.deliveryId.value,
                status = DeliveryStatusDto.from(state.status),
                driverId = state.driverId?.value,
            )
    }
}

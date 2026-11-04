package com.pl.platform.delivery.application.port

import com.pl.platform.delivery.domain.Delivery
import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.domain.DeliveryStatus
import com.pl.platform.delivery.domain.DriverId

/** State of a delivery after a transition; result of confirm, assigndriver, pickup, start, complete and cancel. */
data class DeliveryState(
    val deliveryId: DeliveryId,
    val status: DeliveryStatus,
    val driverId: DriverId?,
) {
    companion object {
        fun from(delivery: Delivery) =
            DeliveryState(
                deliveryId = delivery.id,
                status = delivery.status,
                driverId = delivery.driverId?.let(::DriverId),
            )
    }
}

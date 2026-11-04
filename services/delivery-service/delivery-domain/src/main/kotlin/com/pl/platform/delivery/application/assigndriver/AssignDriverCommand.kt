package com.pl.platform.delivery.application.assigndriver

import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.domain.DriverId

/** [driverId] null means: pick a random driver. */
data class AssignDriverCommand(
    val deliveryId: DeliveryId,
    val driverId: DriverId?,
)

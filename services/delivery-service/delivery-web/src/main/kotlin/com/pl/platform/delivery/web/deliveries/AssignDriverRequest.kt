package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.assigndriver.AssignDriverCommand
import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.domain.DriverId
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class AssignDriverRequest(
    @field:Schema(description = "Driver to assign. Omit (or send no body) to pick a random driver.")
    val driverId: UUID? = null,
) {
    fun toCommand(deliveryId: UUID) = AssignDriverCommand(DeliveryId(deliveryId), driverId?.let(::DriverId))
}

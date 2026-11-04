package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.create.CreateDeliveryResult
import java.math.BigDecimal
import java.util.UUID

data class CreateDeliveryResponse(
    val id: UUID,
    val price: BigDecimal,
    val currency: String,
    val status: DeliveryStatusDto,
) {
    companion object {
        fun from(result: CreateDeliveryResult) =
            CreateDeliveryResponse(
                id = result.deliveryId.value,
                price = result.price,
                currency = result.currency,
                status = DeliveryStatusDto.from(result.status),
            )
    }
}

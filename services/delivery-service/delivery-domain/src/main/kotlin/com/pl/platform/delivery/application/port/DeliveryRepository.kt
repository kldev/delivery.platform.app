package com.pl.platform.delivery.application.port

import com.pl.platform.delivery.domain.Delivery
import com.pl.platform.delivery.domain.DeliveryId

interface DeliveryRepository {
    fun findById(id: DeliveryId): Delivery?

    fun create(delivery: Delivery)

    fun update(delivery: Delivery)
}

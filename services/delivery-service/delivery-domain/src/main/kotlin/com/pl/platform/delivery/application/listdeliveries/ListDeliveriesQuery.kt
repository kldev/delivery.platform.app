package com.pl.platform.delivery.application.listdeliveries

import com.pl.platform.delivery.application.port.PageQuery
import com.pl.platform.delivery.domain.DeliveryStatus
import java.util.UUID

data class ListDeliveriesQuery(
    val status: DeliveryStatus? = null,
    val deliveryId: UUID? = null,
    val driverId: UUID? = null,
    val page: PageQuery = PageQuery(),
)

package com.pl.platform.delivery.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesQuery
import java.util.UUID

interface DeliveryQueryRepository {
    fun findById(id: UUID): DeliveryDetails?

    /** Newest deliveries first. */
    fun search(query: ListDeliveriesQuery): SliceResponse<DeliveryDetails>
}

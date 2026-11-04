package com.pl.platform.delivery.application.listdeliveries

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.port.DeliveryDetails
import com.pl.platform.delivery.application.port.DeliveryQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListDeliveriesHandler(
    private val deliveryQueryRepository: DeliveryQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListDeliveriesQuery): SliceResponse<DeliveryDetails> = deliveryQueryRepository.search(query)
}

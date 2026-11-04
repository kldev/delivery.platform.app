package com.pl.platform.delivery.application.getdelivery

import com.pl.platform.delivery.application.port.DeliveryDetails
import com.pl.platform.delivery.application.port.DeliveryQueryRepository
import com.pl.platform.delivery.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetDeliveryHandler(
    private val deliveryQueryRepository: DeliveryQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: GetDeliveryQuery): DeliveryDetails =
        deliveryQueryRepository.findById(query.deliveryId.value)
            ?: throw NotFoundException("delivery", query.deliveryId.value)
}

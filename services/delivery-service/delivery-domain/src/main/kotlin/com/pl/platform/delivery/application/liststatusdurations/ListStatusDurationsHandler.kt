package com.pl.platform.delivery.application.liststatusdurations

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.port.DeliveryStatusDurationQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListStatusDurationsHandler(
    private val statusDurationQueryRepository: DeliveryStatusDurationQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListStatusDurationsQuery): SliceResponse<DeliveryStatusDuration> =
        statusDurationQueryRepository.search(query)
}

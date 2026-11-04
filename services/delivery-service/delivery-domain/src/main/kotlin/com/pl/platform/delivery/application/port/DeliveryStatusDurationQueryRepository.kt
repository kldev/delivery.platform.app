package com.pl.platform.delivery.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.liststatusdurations.DeliveryStatusDuration
import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsQuery

interface DeliveryStatusDurationQueryRepository {
    /** Oldest status first. */
    fun search(query: ListStatusDurationsQuery): SliceResponse<DeliveryStatusDuration>
}

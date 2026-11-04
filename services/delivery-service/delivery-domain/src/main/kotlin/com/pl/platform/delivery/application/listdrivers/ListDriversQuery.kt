package com.pl.platform.delivery.application.listdrivers

import com.pl.platform.delivery.domain.DriverId

/** [driverId] narrows the list to one driver. */
data class ListDriversQuery(
    val driverId: DriverId? = null,
)

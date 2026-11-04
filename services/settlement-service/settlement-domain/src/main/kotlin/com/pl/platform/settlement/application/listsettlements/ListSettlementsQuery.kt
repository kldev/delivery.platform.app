package com.pl.platform.settlement.application.listsettlements

import com.pl.platform.settlement.application.port.PageQuery
import java.util.UUID

data class ListSettlementsQuery(
    /** Part of the driver's name, case-insensitive. */
    val search: String? = null,
    val driverId: UUID? = null,
    val deliveryId: UUID? = null,
    val page: PageQuery = PageQuery(),
)

package com.pl.platform.settlement.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.settlement.application.listsettlements.ListSettlementsQuery
import java.util.UUID

interface SettlementQueryRepository {
    fun findById(id: UUID): SettlementDetails?

    /** Newest first. */
    fun search(query: ListSettlementsQuery): SliceResponse<SettlementDetails>
}

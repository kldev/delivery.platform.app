package com.pl.platform.settlement.application.listsettlements

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.settlement.application.port.SettlementDetails
import com.pl.platform.settlement.application.port.SettlementQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListSettlementsHandler(
    private val settlements: SettlementQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListSettlementsQuery): SliceResponse<SettlementDetails> = settlements.search(query)
}

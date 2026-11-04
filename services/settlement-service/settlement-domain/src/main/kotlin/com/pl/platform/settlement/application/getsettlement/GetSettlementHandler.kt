package com.pl.platform.settlement.application.getsettlement

import com.pl.platform.settlement.application.port.SettlementDetails
import com.pl.platform.settlement.application.port.SettlementQueryRepository
import com.pl.platform.settlement.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetSettlementHandler(
    private val settlements: SettlementQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: GetSettlementQuery): SettlementDetails =
        settlements.findById(query.id) ?: throw NotFoundException("settlement", query.id)
}

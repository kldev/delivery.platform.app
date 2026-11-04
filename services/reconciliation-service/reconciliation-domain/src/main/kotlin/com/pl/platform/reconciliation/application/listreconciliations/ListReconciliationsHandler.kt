package com.pl.platform.reconciliation.application.listreconciliations

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.reconciliation.application.port.ReconciliationDetails
import com.pl.platform.reconciliation.application.port.ReconciliationQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListReconciliationsHandler(
    private val reconciliations: ReconciliationQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListReconciliationsQuery): SliceResponse<ReconciliationDetails> = reconciliations.search(query)
}

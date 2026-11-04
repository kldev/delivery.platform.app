package com.pl.platform.reconciliation.application.getreconciliation

import com.pl.platform.reconciliation.application.port.ReconciliationDetails
import com.pl.platform.reconciliation.application.port.ReconciliationQueryRepository
import com.pl.platform.reconciliation.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetReconciliationHandler(
    private val reconciliations: ReconciliationQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: GetReconciliationQuery): ReconciliationDetails =
        reconciliations.findById(query.id) ?: throw NotFoundException("reconciliation", query.id)
}

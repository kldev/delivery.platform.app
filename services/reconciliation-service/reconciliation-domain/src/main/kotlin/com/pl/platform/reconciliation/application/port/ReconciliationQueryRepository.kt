package com.pl.platform.reconciliation.application.port

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.reconciliation.application.listreconciliations.ListReconciliationsQuery
import java.util.UUID

interface ReconciliationQueryRepository {
    fun findById(id: UUID): ReconciliationDetails?

    /** Newest first. */
    fun search(query: ListReconciliationsQuery): SliceResponse<ReconciliationDetails>
}

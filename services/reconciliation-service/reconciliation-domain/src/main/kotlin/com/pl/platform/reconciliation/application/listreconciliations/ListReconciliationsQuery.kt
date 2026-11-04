package com.pl.platform.reconciliation.application.listreconciliations

import com.pl.platform.reconciliation.application.port.PageQuery
import com.pl.platform.reconciliation.domain.ReconciliationStatus
import java.util.UUID

data class ListReconciliationsQuery(
    val status: ReconciliationStatus? = null,
    val deliveryId: UUID? = null,
    val settlementId: UUID? = null,
    val paymentId: UUID? = null,
    /** Exact match: the id of the transfer at the payment provider. */
    val externalTransactionId: String? = null,
    val page: PageQuery = PageQuery(),
)

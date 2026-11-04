package com.pl.platform.reconciliation.application.port

import com.pl.platform.reconciliation.domain.ReconciliationStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Read model shared by getreconciliation and listreconciliations. */
data class ReconciliationDetails(
    val id: UUID,
    val deliveryId: UUID,
    val settlementId: UUID?,
    val paymentId: UUID?,
    val externalTransactionId: String?,
    val expectedAmount: BigDecimal?,
    val actualAmount: BigDecimal?,
    val difference: BigDecimal?,
    val currency: String,
    val status: ReconciliationStatus,
    val createdAt: Instant,
    val reconciledAt: Instant?,
)

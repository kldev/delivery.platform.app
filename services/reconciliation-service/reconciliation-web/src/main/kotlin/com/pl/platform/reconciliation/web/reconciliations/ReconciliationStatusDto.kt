package com.pl.platform.reconciliation.web.reconciliations

import com.pl.platform.reconciliation.domain.ReconciliationStatus

/** API status, mapped explicitly: renaming a domain constant must not silently change the contract. */
enum class ReconciliationStatusDto {
    PENDING,
    RECONCILED,
    DISCREPANCY,
    MISSING_PAYMENT,
    MISSING_SETTLEMENT,
    ;

    fun toDomain(): ReconciliationStatus =
        when (this) {
            PENDING -> ReconciliationStatus.PENDING
            RECONCILED -> ReconciliationStatus.RECONCILED
            DISCREPANCY -> ReconciliationStatus.DISCREPANCY
            MISSING_PAYMENT -> ReconciliationStatus.MISSING_PAYMENT
            MISSING_SETTLEMENT -> ReconciliationStatus.MISSING_SETTLEMENT
        }

    companion object {
        fun from(status: ReconciliationStatus): ReconciliationStatusDto =
            when (status) {
                ReconciliationStatus.PENDING -> PENDING
                ReconciliationStatus.RECONCILED -> RECONCILED
                ReconciliationStatus.DISCREPANCY -> DISCREPANCY
                ReconciliationStatus.MISSING_PAYMENT -> MISSING_PAYMENT
                ReconciliationStatus.MISSING_SETTLEMENT -> MISSING_SETTLEMENT
            }
    }
}

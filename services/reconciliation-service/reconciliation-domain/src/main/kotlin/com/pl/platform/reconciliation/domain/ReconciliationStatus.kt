package com.pl.platform.reconciliation.domain

/**
 * PENDING until both settlement and payment are known. MISSING_PAYMENT and MISSING_SETTLEMENT are allowed by the
 * schema but nothing sets them yet: that needs a time limit for the missing side (not decided).
 */
enum class ReconciliationStatus {
    PENDING,
    RECONCILED,
    DISCREPANCY,
    MISSING_PAYMENT,
    MISSING_SETTLEMENT,
}

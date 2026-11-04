package com.pl.platform.ledger.domain

import java.math.BigDecimal
import java.util.UUID

class LedgerEntry(
    val id: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: LedgerEntryType,
) {

    init {
        require(amount != BigDecimal.ZERO) {
            "Ledger entry amount must not be zero"
        }
    }

    fun signedAmount(): BigDecimal =
        when (type) {
            LedgerEntryType.DEBIT -> amount.negate()
            LedgerEntryType.CREDIT -> amount
        }
}
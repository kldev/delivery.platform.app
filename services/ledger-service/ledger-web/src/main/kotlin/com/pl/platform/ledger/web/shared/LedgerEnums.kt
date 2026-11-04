package com.pl.platform.ledger.web.shared

import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import com.pl.platform.ledger.domain.LedgerEntryType
import com.pl.platform.ledger.domain.LedgerTransactionType

// API enums, mapped explicitly: renaming a domain constant must not silently change the contract.

enum class AccountOwnerTypeDto {
    PLATFORM,
    DRIVER,
    ;

    fun toDomain(): LedgerAccountOwnerType =
        when (this) {
            PLATFORM -> LedgerAccountOwnerType.PLATFORM
            DRIVER -> LedgerAccountOwnerType.DRIVER
        }

    companion object {
        fun from(type: LedgerAccountOwnerType): AccountOwnerTypeDto =
            when (type) {
                LedgerAccountOwnerType.PLATFORM -> PLATFORM
                LedgerAccountOwnerType.DRIVER -> DRIVER
            }
    }
}

enum class EntryTypeDto {
    DEBIT,
    CREDIT,
    ;

    companion object {
        fun from(type: LedgerEntryType): EntryTypeDto =
            when (type) {
                LedgerEntryType.DEBIT -> DEBIT
                LedgerEntryType.CREDIT -> CREDIT
            }
    }
}

enum class TransactionTypeDto {
    DRIVER_SETTLEMENT,
    DRIVER_PAYOUT,
    ADJUSTMENT,
    ;

    companion object {
        fun from(type: LedgerTransactionType): TransactionTypeDto =
            when (type) {
                LedgerTransactionType.DRIVER_SETTLEMENT -> DRIVER_SETTLEMENT
                LedgerTransactionType.DRIVER_PAYOUT -> DRIVER_PAYOUT
                LedgerTransactionType.ADJUSTMENT -> ADJUSTMENT
            }
    }
}

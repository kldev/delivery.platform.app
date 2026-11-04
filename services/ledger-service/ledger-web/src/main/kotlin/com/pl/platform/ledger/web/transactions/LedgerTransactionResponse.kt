package com.pl.platform.ledger.web.transactions

import com.pl.platform.ledger.application.listtransactions.LedgerTransactionDetails
import com.pl.platform.ledger.web.shared.EntryTypeDto
import com.pl.platform.ledger.web.shared.TransactionTypeDto
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class LedgerTransactionResponse(
    val id: UUID,
    val type: TransactionTypeDto,
    val referenceType: String,
    val referenceId: UUID,
    val currency: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val entries: List<LedgerTransactionEntryResponse>,
) {
    companion object {
        fun from(transaction: LedgerTransactionDetails) =
            LedgerTransactionResponse(
                id = transaction.id,
                type = TransactionTypeDto.from(transaction.type),
                referenceType = transaction.referenceType,
                referenceId = transaction.referenceId,
                currency = transaction.currency,
                occurredAt = transaction.occurredAt,
                createdAt = transaction.createdAt,
                entries = transaction.entries.map(LedgerTransactionEntryResponse::from),
            )
    }
}

data class LedgerTransactionEntryResponse(
    val id: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: EntryTypeDto,
) {
    companion object {
        fun from(entry: LedgerTransactionDetails.Entry) =
            LedgerTransactionEntryResponse(
                id = entry.id,
                accountId = entry.accountId,
                amount = entry.amount,
                type = EntryTypeDto.from(entry.type),
            )
    }
}

package com.pl.platform.ledger.web.entries

import com.pl.platform.ledger.application.listentries.LedgerEntryItem
import com.pl.platform.ledger.web.shared.EntryTypeDto
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class LedgerEntryResponse(
    val id: UUID,
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: EntryTypeDto,
    val createdAt: Instant,
) {
    companion object {
        fun from(entry: LedgerEntryItem) =
            LedgerEntryResponse(
                id = entry.id,
                transactionId = entry.transactionId,
                accountId = entry.accountId,
                amount = entry.amount,
                type = EntryTypeDto.from(entry.type),
                createdAt = entry.createdAt,
            )
    }
}

package com.pl.platform.ledger.application.listtransactions

import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.domain.LedgerEntryType
import com.pl.platform.ledger.domain.LedgerTransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** [from] and [to] are calendar days (of occurredAt) in [timezone], both inclusive. */
data class ListLedgerTransactionsQuery(
    val referenceId: UUID? = null,
    val referenceType: String? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val timezone: ZoneId = ZoneId.of("Europe/Warsaw"),
    val page: PageQuery = PageQuery(),
) {
    fun fromAtInstant(): Instant? = from?.atStartOfDay(timezone)?.toInstant()

    fun toAtInstant(): Instant? = to?.plusDays(1)?.atStartOfDay(timezone)?.toInstant()
}

data class LedgerTransactionDetails(
    val id: UUID,
    val type: LedgerTransactionType,
    val referenceType: String,
    val referenceId: UUID,
    val currency: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val entries: List<Entry>,
) {
    data class Entry(
        val id: UUID,
        val accountId: UUID,
        val amount: BigDecimal,
        val type: LedgerEntryType,
    )
}

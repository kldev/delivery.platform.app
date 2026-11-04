package com.pl.platform.ledger.application.listentries

import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.domain.LedgerEntryType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** [from] and [to] are calendar days in [timezone], both inclusive. */
data class ListLedgerEntriesQuery(
    val accountId: UUID? = null,
    val transactionId: UUID? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val timezone: ZoneId = ZoneId.of("Europe/Warsaw"),
    val page: PageQuery = PageQuery(),
) {
    fun fromAtInstant(): Instant? = from?.atStartOfDay(timezone)?.toInstant()

    fun toAtInstant(): Instant? = to?.plusDays(1)?.atStartOfDay(timezone)?.toInstant()
}

data class LedgerEntryItem(
    val id: UUID,
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val type: LedgerEntryType,
    val createdAt: Instant,
)

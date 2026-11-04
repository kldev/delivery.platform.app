package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerEntry

fun LedgerEntryJpaEntity.toDomain(): LedgerEntry =
    LedgerEntry(
        id = id,
        accountId = accountId,
        amount = amount,
        type = type
    )
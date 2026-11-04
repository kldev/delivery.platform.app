package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerAccount
import com.pl.platform.ledger.domain.LedgerAccountOwnerType

fun LedgerAccountJpaEntity.toDomain(): LedgerAccount =
    when (ownerType) {
        LedgerAccountOwnerType.PLATFORM ->
            LedgerAccount.platform(
                id = id,
                name = name,
                currency = currency,
            )

        LedgerAccountOwnerType.DRIVER ->
            LedgerAccount.driver(
                id = id,
                name = name,
                driverId = requireNotNull(ownerId),
                currency = currency,
            )
    }

fun LedgerAccount.toJpaEntity(): LedgerAccountJpaEntity =
    LedgerAccountJpaEntity(
        id = id,
        name = name,
        ownerType = ownerType,
        ownerId = ownerId,
        currency = currency,
    )
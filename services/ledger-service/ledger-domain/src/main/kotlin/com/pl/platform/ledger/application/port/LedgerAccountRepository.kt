package com.pl.platform.ledger.application.port

import com.pl.platform.ledger.domain.LedgerAccount
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import java.util.UUID

interface LedgerAccountRepository {
    fun findByOwnerAndCurrency(
        ownerType: LedgerAccountOwnerType,
        ownerId: UUID?,
        currency: String,
    ): LedgerAccount?

    fun save(account: LedgerAccount): LedgerAccount
}

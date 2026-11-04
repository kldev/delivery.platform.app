package com.pl.platform.ledger.application.listaccounts

import com.pl.platform.ledger.application.port.PageQuery
import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import java.util.UUID

data class ListLedgerAccountsQuery(
    val ownerType: LedgerAccountOwnerType? = null,
    val ownerId: UUID? = null,
    val currency: String? = null,
    val page: PageQuery = PageQuery(),
)

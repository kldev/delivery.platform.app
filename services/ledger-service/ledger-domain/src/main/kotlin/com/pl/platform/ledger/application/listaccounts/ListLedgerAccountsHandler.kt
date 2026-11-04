package com.pl.platform.ledger.application.listaccounts

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.ledger.application.port.LedgerAccountDetails
import com.pl.platform.ledger.application.port.LedgerAccountQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListLedgerAccountsHandler(
    private val accounts: LedgerAccountQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: ListLedgerAccountsQuery): SliceResponse<LedgerAccountDetails> = accounts.search(query)
}

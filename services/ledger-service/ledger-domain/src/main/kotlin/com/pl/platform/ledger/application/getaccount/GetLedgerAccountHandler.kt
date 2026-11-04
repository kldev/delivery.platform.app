package com.pl.platform.ledger.application.getaccount

import com.pl.platform.ledger.application.port.LedgerAccountDetails
import com.pl.platform.ledger.application.port.LedgerAccountQueryRepository
import com.pl.platform.ledger.domain.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetLedgerAccountHandler(
    private val accounts: LedgerAccountQueryRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: GetLedgerAccountQuery): LedgerAccountDetails =
        accounts.findById(query.accountId) ?: throw NotFoundException("ledger-account", query.accountId)
}

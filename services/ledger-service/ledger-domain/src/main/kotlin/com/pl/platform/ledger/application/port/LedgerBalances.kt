package com.pl.platform.ledger.application.port

import com.pl.platform.ledger.domain.LedgerTransaction

/** Running balance per account (credits minus debits). Must be called in the transaction that posts. */
interface LedgerBalances {
    fun apply(transaction: LedgerTransaction)
}

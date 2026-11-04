package com.pl.platform.ledger.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataLedgerEntryRepository :
    JpaRepository<LedgerEntryJpaEntity, UUID> {

    fun findAllByTransactionId(
        transactionId: UUID,
    ): List<LedgerEntryJpaEntity>

    fun findAllByAccountIdOrderByCreatedAtAsc(
        accountId: UUID,
    ): List<LedgerEntryJpaEntity>
}
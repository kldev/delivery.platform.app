package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.application.port.LedgerTransactionRepository
import com.pl.platform.ledger.domain.LedgerTransaction
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaLedgerTransactionRepository(
    private val repository: SpringDataLedgerTransactionRepository,
) : LedgerTransactionRepository {

    override fun existsByReference(
        referenceType: String,
        referenceId: UUID,
    ): Boolean =
        repository.existsByReferenceTypeAndReferenceId(
            referenceType = referenceType,
            referenceId = referenceId,
        )

    override fun save(
        transaction: LedgerTransaction,
    ): LedgerTransaction =
        repository
            .save(transaction.toJpaEntity())
            .toDomain()
}
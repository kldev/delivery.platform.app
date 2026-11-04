package com.pl.platform.ledger.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SpringDataLedgerTransactionRepository :
    JpaRepository<LedgerTransactionJpaEntity, UUID> {

    fun findByReferenceTypeAndReferenceId(
        referenceType: String,
        referenceId: UUID,
    ): LedgerTransactionJpaEntity?

    fun existsByReferenceTypeAndReferenceId(
        referenceType: String,
        referenceId: UUID,
    ): Boolean

    @Query(
        """
    SELECT DISTINCT t
    FROM LedgerTransactionJpaEntity t
    LEFT JOIN FETCH t.entries
    WHERE t.referenceType = :referenceType
      AND t.referenceId = :referenceId
    """
    )
    fun findByReferenceTypeAndReferenceIdWithEntries(
        @Param("referenceType") referenceType: String,
        @Param("referenceId") referenceId: UUID,
    ): LedgerTransactionJpaEntity?
}
package com.pl.platform.ledger.infrastructure.persistence

import com.pl.platform.ledger.domain.LedgerAccountOwnerType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataLedgerAccountRepository :
    JpaRepository<LedgerAccountJpaEntity, UUID> {

    fun findByOwnerTypeAndOwnerIdAndCurrency(
        ownerType: LedgerAccountOwnerType,
        ownerId: UUID?,
        currency: String,
    ): LedgerAccountJpaEntity?

    fun findByOwnerTypeAndCurrency(
        ownerType: LedgerAccountOwnerType,
        currency: String,
    ): LedgerAccountJpaEntity?
}
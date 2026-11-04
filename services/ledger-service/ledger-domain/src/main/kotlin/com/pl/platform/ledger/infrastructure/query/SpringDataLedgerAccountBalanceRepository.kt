package com.pl.platform.ledger.infrastructure.query

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataLedgerAccountBalanceRepository : JpaRepository<LedgerAccountBalanceJpaEntity, UUID>
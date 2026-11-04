package com.pl.platform.settlement.infrastructure.persistence

import com.pl.platform.settlement.application.port.SettlementRateRepository
import com.pl.platform.settlement.domain.SettlementRate
import org.springframework.stereotype.Component

@Component
class JpaSettlementRateRepository(
    private val repository: SpringDataSettlementRateRepository,
) : SettlementRateRepository {

    override fun findActive(): List<SettlementRate> =
        repository
            .findAllByActiveTrue()
            .map(SettlementRateJpaEntity::toDomain)
}
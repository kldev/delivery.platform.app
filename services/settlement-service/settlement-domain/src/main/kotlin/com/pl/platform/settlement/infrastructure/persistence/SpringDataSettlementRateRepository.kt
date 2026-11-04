package com.pl.platform.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataSettlementRateRepository :
    JpaRepository<SettlementRateJpaEntity, String> {

    fun findAllByActiveTrue(): List<SettlementRateJpaEntity>
}
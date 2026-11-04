package com.pl.platform.settlement.application.port

import com.pl.platform.settlement.domain.SettlementRate

interface SettlementRateRepository {

    fun findActive(): List<SettlementRate>
}
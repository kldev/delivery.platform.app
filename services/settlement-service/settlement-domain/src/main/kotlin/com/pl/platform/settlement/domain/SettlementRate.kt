package com.pl.platform.settlement.domain

import java.math.BigDecimal
import java.time.Instant

data class SettlementRate(
    val code: String,
    val name: String,
    val percentage: BigDecimal,
    val active: Boolean,
    val createdAt: Instant,
)
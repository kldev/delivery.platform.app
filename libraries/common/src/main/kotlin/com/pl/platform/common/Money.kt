package com.pl.platform.common

import java.math.BigDecimal

data class Money (
    val amount: BigDecimal,
    val currency: String
)
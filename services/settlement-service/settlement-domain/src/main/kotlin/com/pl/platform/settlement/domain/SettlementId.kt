package com.pl.platform.settlement.domain

import java.util.UUID

@JvmInline
value class SettlementId(
    val value: UUID
) {
    companion object {
        fun new(): SettlementId =
            SettlementId(UUID.randomUUID())
    }
}
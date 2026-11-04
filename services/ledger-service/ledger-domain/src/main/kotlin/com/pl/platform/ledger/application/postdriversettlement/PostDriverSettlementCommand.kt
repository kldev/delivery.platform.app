package com.pl.platform.ledger.application.postdriversettlement

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/** Built from DriverSettlementCompletedEvent; [eventId] makes the consumer idempotent. */
data class PostDriverSettlementCommand(
    val eventId: UUID,
    val eventType: String,
    val settlementId: UUID,
    val driverId: UUID,
    val driverFullName: String,
    val currency: String,
    val amount: BigDecimal,
    val occurredAt: Instant,
)

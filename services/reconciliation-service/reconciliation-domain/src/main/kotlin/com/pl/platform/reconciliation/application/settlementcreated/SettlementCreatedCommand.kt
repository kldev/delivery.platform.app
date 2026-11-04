package com.pl.platform.reconciliation.application.settlementcreated

import java.math.BigDecimal
import java.util.UUID

/**
 * From SettlementCreatedEvent; [expectedAmount] is the delivery's price (deliveryAmount) - what the customer should
 * have paid. Not the driver's share (driverAmount): the payment is the customer's money, the driver gets a part of it.
 */
data class SettlementCreatedCommand(
    val eventId: UUID,
    val eventType: String,
    val settlementId: UUID,
    val deliveryId: UUID,
    val expectedAmount: BigDecimal,
    val currency: String,
)

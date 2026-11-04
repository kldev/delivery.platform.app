package com.pl.platform.settlement.events

import com.pl.platform.common.messaging.event.settlement.DriverSettlementCompletedEvent
import com.pl.platform.common.messaging.event.settlement.SettlementCreatedEvent
import com.pl.platform.settlement.domain.Settlement

fun Settlement.toCreatedEvent(): SettlementCreatedEvent =
    SettlementCreatedEvent(
        settlementId = id.value,
        deliveryId = deliveryId,
        driverId = driverId,
        driverFullName = driverFullName,
        deliveryAmount = deliveryAmount,
        currency = currency,
        basePercentage = basePercentage,
        nightPercentage = nightPercentage,
        weekendPercentage = weekendPercentage,
        distancePercentage = distancePercentage,
        totalPercentage = totalPercentage,
        driverAmount = driverAmount,
        distanceKm = distanceKm,
        completedAt = completedAt,
    )

/** What the ledger posts to the driver's account: the driver's share, not the price of the delivery. */
fun Settlement.toDriverSettlementCompletedEvent(): DriverSettlementCompletedEvent =
    DriverSettlementCompletedEvent(
        settlementId = id.value,
        driverId = driverId,
        amount = driverAmount,
        currency = currency,
        driverFullName = driverFullName,
        deliveryId = deliveryId,
    )

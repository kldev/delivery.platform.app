package com.pl.platform.settlement.application.deliverycompleted

import com.pl.platform.common.messaging.port.OutboxRepository
import com.pl.platform.settlement.application.port.ProcessedEvents
import com.pl.platform.settlement.application.port.SettlementRateRepository
import com.pl.platform.settlement.application.port.SettlementRepository
import com.pl.platform.settlement.domain.SettlementCalculator
import com.pl.platform.settlement.events.toCreatedEvent
import com.pl.platform.settlement.events.toDriverSettlementCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

/**
 * Settles a completed delivery: computes the driver's share and announces it (settlement.created for
 * reconciliation, driver.settlement.completed for the ledger) through the outbox, in one transaction.
 * One settlement per delivery: a delivery that is already settled is skipped, whatever the event id.
 */
@Service
class DeliveryCompletedHandler(
    private val settlements: SettlementRepository,
    private val rates: SettlementRateRepository,
    private val processedEvents: ProcessedEvents,
    private val outbox: OutboxRepository,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: DeliveryCompletedCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }

        if (settlements.existsByDeliveryId(command.deliveryId)) {
            log.info("Delivery {} is already settled, skipping event {}", command.deliveryId, command.eventId)
        } else {
            val settlement =
                SettlementCalculator.calculate(
                    deliveryId = command.deliveryId,
                    driverId = command.driverId,
                    driverFullName = command.driverFullName,
                    deliveryAmount = command.deliveryAmount,
                    currency = command.currency,
                    distanceKm = command.distanceKm,
                    completedAt = command.completedAt,
                    rates = rates.findActive(),
                    now = Instant.now(clock),
                )
            settlements.create(settlement)
            outbox.save(settlement.toCreatedEvent())
            outbox.save(settlement.toDriverSettlementCompletedEvent())
        }

        processedEvents.markProcessed(command.eventId, command.eventType)
    }
}

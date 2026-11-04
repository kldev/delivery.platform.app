package com.pl.platform.reconciliation.application.settlementcreated

import com.pl.platform.reconciliation.application.port.ProcessedEvents
import com.pl.platform.reconciliation.application.port.ReconciliationRepository
import com.pl.platform.reconciliation.domain.Reconciliation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class SettlementCreatedHandler(
    private val reconciliations: ReconciliationRepository,
    private val processedEvents: ProcessedEvents,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: SettlementCreatedCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }
        val now = Instant.now(clock)

        val existing = reconciliations.findByDeliveryIdForUpdate(command.deliveryId)
        if (existing == null) {
            reconciliations.create(
                Reconciliation.fromSettlement(
                    deliveryId = command.deliveryId,
                    settlementId = command.settlementId,
                    expectedAmount = command.expectedAmount,
                    currency = command.currency,
                    now = now,
                ),
            )
        } else {
            existing.applySettlement(command.settlementId, command.expectedAmount, command.currency, now)
            reconciliations.update(existing)
            log.info("Reconciliation of delivery {} is {}", existing.deliveryId, existing.status)
        }

        processedEvents.markProcessed(command.eventId, command.eventType)
    }
}

package com.pl.platform.reconciliation.application.paymentcompleted

import com.pl.platform.reconciliation.application.port.ProcessedEvents
import com.pl.platform.reconciliation.application.port.ReconciliationRepository
import com.pl.platform.reconciliation.domain.Reconciliation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class PaymentCompletedHandler(
    private val reconciliations: ReconciliationRepository,
    private val processedEvents: ProcessedEvents,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handle(command: PaymentCompletedCommand) {
        if (processedEvents.isProcessed(command.eventId)) {
            log.info("Event {} already processed, skipping", command.eventId)
            return
        }
        val now = Instant.now(clock)

        val existing = reconciliations.findByDeliveryIdForUpdate(command.deliveryId)
        if (existing == null) {
            reconciliations.create(
                Reconciliation.fromPayment(
                    deliveryId = command.deliveryId,
                    paymentId = command.paymentId,
                    externalTransactionId = command.externalTransactionId,
                    actualAmount = command.amount,
                    currency = command.currency,
                    now = now,
                ),
            )
        } else {
            existing.applyPayment(command.paymentId, command.externalTransactionId, command.amount, command.currency, now)
            reconciliations.update(existing)
            log.info("Reconciliation of delivery {} is {}", existing.deliveryId, existing.status)
        }

        processedEvents.markProcessed(command.eventId, command.eventType)
    }
}

package com.pl.platform.reconciliation.testing

import com.pl.platform.reconciliation.application.paymentcompleted.PaymentCompletedCommand
import com.pl.platform.reconciliation.application.paymentcompleted.PaymentCompletedHandler
import com.pl.platform.reconciliation.application.settlementcreated.SettlementCreatedCommand
import com.pl.platform.reconciliation.application.settlementcreated.SettlementCreatedHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

/** Feeds events through the real actions and reads the stored reconciliation with SQL. */
@Component
class ReconciliationEvents(
    private val settlements: SettlementCreatedHandler,
    private val payments: PaymentCompletedHandler,
    private val jdbc: JdbcTemplate,
) {
    fun settlement(
        deliveryId: UUID,
        expected: String,
        currency: String = "PLN",
        eventId: UUID = UUID.randomUUID(),
        settlementId: UUID = UUID.randomUUID(),
    ) = SettlementCreatedCommand(eventId, "settlement.created", settlementId, deliveryId, BigDecimal(expected), currency)
        .also(settlements::handle)

    fun payment(
        deliveryId: UUID,
        amount: String,
        currency: String = "PLN",
        eventId: UUID = UUID.randomUUID(),
        paymentId: UUID = UUID.randomUUID(),
    ) = PaymentCompletedCommand(eventId, "payment.completed", paymentId, deliveryId, "TX-$paymentId", BigDecimal(amount), currency)
        .also(payments::handle)

    fun stateOf(deliveryId: UUID): Map<String, Any?> =
        jdbc.queryForMap(
            "SELECT status, expected_amount, actual_amount, difference, settlement_id, payment_id, reconciled_at FROM reconciliations WHERE delivery_id = ?",
            deliveryId,
        )

    fun count(deliveryId: UUID): Int =
        jdbc.queryForObject("SELECT count(*) FROM reconciliations WHERE delivery_id = ?", Int::class.java, deliveryId)!!
}

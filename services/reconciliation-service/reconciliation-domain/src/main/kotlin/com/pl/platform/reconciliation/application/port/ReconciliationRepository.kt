package com.pl.platform.reconciliation.application.port

import com.pl.platform.reconciliation.domain.Reconciliation
import java.util.UUID

interface ReconciliationRepository {
    /** Locks the row (SELECT ... FOR UPDATE): settlement and payment of one delivery may arrive at the same time. */
    fun findByDeliveryIdForUpdate(deliveryId: UUID): Reconciliation?

    fun create(reconciliation: Reconciliation)

    fun update(reconciliation: Reconciliation)
}

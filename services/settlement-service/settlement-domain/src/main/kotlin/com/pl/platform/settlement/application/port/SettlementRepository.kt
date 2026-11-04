package com.pl.platform.settlement.application.port

import com.pl.platform.settlement.domain.Settlement
import java.util.UUID

interface SettlementRepository {
    fun existsByDeliveryId(deliveryId: UUID): Boolean

    fun exists(): Boolean

    fun create(settlement: Settlement)
}

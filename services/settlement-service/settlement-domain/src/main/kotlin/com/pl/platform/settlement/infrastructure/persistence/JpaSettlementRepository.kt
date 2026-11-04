package com.pl.platform.settlement.infrastructure.persistence

import com.pl.platform.settlement.application.port.SettlementRepository
import com.pl.platform.settlement.domain.Settlement
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaSettlementRepository(
    private val repository: SpringDataSettlementRepository,
) : SettlementRepository {
    override fun existsByDeliveryId(deliveryId: UUID): Boolean = repository.existsByDeliveryId(deliveryId)

    override fun exists(): Boolean = repository.count() > 0

    // Flushed now: two events for one delivery racing past existsByDeliveryId fail here on the unique
    // delivery_id; the Kafka error handler retries the loser, which then finds the settlement and skips.
    override fun create(settlement: Settlement) {
        repository.saveAndFlush(SettlementJpaEntity.create(settlement))
    }
}

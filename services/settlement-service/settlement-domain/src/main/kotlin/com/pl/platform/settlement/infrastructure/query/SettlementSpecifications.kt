package com.pl.platform.settlement.infrastructure.query

import com.pl.platform.settlement.application.listsettlements.ListSettlementsQuery
import com.pl.platform.settlement.infrastructure.persistence.SettlementJpaEntity
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

object SettlementSpecifications {
    private const val ESCAPE = '\\'

    fun build(query: ListSettlementsQuery): Specification<SettlementJpaEntity> =
        listOfNotNull(
            query.search?.takeIf { it.isNotBlank() }?.let(::driverNameContains),
            query.driverId?.let { equal("driverId", it) },
            query.deliveryId?.let { equal("deliveryId", it) },
        ).fold(Specification { _, _, _ -> null }) { result, specification -> result.and(specification) }

    private fun equal(
        attribute: String,
        value: UUID,
    ): Specification<SettlementJpaEntity> = Specification { root, _, cb -> cb.equal(root.get<UUID>(attribute), value) }

    // % and _ typed by the user are literal characters, not wildcards.
    private fun driverNameContains(search: String): Specification<SettlementJpaEntity> {
        val escaped =
            search
                .trim()
                .lowercase()
                .replace("$ESCAPE", "$ESCAPE$ESCAPE")
                .replace("%", "$ESCAPE%")
                .replace("_", "${ESCAPE}_")
        return Specification { root, _, cb -> cb.like(cb.lower(root.get("driverFullName")), "%$escaped%", ESCAPE) }
    }
}

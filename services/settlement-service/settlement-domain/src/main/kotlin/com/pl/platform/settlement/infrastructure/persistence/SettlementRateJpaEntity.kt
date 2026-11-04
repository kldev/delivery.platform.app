package com.pl.platform.settlement.infrastructure.persistence

import com.pl.platform.settlement.domain.SettlementRate
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "settlement_rates")
class SettlementRateJpaEntity(

    @Id
    @Column(name = "code", length = 50, nullable = false)
    var code: String,

    @Column(name = "name", length = 100, nullable = false)
    var name: String,

    @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
    var percentage: BigDecimal,

    @Column(name = "active", nullable = false)
    var active: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
) {

    fun toDomain(): SettlementRate =
        SettlementRate(
            code = code,
            name = name,
            percentage = percentage,
            active = active,
            createdAt = createdAt,
        )
}
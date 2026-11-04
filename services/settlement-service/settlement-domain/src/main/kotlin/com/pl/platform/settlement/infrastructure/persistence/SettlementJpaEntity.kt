package com.pl.platform.settlement.infrastructure.persistence

import com.pl.platform.settlement.domain.Settlement
import com.pl.platform.settlement.domain.SettlementId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "settlements")
class SettlementJpaEntity(

    @Id
    var id: UUID,

    @Column(name = "delivery_id", nullable = false)
    var deliveryId: UUID,

    @Column(name = "driver_id", nullable = false)
    var driverId: UUID,

    @Column(name = "driver_full_name", nullable = false, length = 500)
    var driverFullName: String,

    @Column(name = "delivery_amount", nullable = false, precision = 12, scale = 2)
    var deliveryAmount: BigDecimal,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String,

    @Column(name = "base_percentage", nullable = false, precision = 5, scale = 2)
    var basePercentage: BigDecimal,

    @Column(name = "night_percentage", nullable = false, precision = 5, scale = 2)
    var nightPercentage: BigDecimal,

    @Column(name = "weekend_percentage", nullable = false, precision = 5, scale = 2)
    var weekendPercentage: BigDecimal,

    @Column(name = "distance_percentage", nullable = false, precision = 5, scale = 2)
    var distancePercentage: BigDecimal,

    @Column(name = "total_percentage", nullable = false, precision = 5, scale = 2)
    var totalPercentage: BigDecimal,

    @Column(name = "driver_amount", nullable = false, precision = 12, scale = 2)
    var driverAmount: BigDecimal,

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    var distanceKm: BigDecimal,

    @Column(name = "completed_at", nullable = false)
    var completedAt: Instant,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
) {

    fun toDomain(): Settlement =
        Settlement(
            id = SettlementId(id),
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
            createdAt = createdAt,
        )

    companion object {

        fun create(
            settlement: Settlement,
        ): SettlementJpaEntity =
            SettlementJpaEntity(
                id = settlement.id.value,
                deliveryId = settlement.deliveryId,
                driverId = settlement.driverId,
                driverFullName = settlement.driverFullName,
                deliveryAmount = settlement.deliveryAmount,
                currency = settlement.currency,
                basePercentage = settlement.basePercentage,
                nightPercentage = settlement.nightPercentage,
                weekendPercentage = settlement.weekendPercentage,
                distancePercentage = settlement.distancePercentage,
                totalPercentage = settlement.totalPercentage,
                driverAmount = settlement.driverAmount,
                distanceKm = settlement.distanceKm,
                completedAt = settlement.completedAt,
                createdAt = settlement.createdAt,
            )
    }
}
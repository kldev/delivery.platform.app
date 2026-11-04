package com.pl.platform.reporting.model

import java.math.BigDecimal
import java.util.*


data class TotalWithCurrency(val total: BigDecimal, val currency: String)


data class DriverReportResult(
    val driverId: UUID,
    val fullName: String? = null,
    val deliveriesCount: Int = 0,
    val deliveriesTotalCost: List<TotalWithCurrency> = emptyList(),
    val settlementsCount: Int = 0,
    val settlementDriverRevenue: List<TotalWithCurrency> = emptyList(),
    val success: Boolean = true
) {

    companion object {
        fun success(
            driverId: UUID,
            fullName: String,
            deliveriesCount: Int,
            deliveriesTotalCost: List<TotalWithCurrency>,
            settlementsCount: Int,
            settlementDriverRevenue: List<TotalWithCurrency>
        ) = DriverReportResult(
            driverId = driverId,
            fullName = fullName,
            deliveriesCount = deliveriesCount,
            deliveriesTotalCost = deliveriesTotalCost,
            settlementsCount = settlementsCount,
            settlementDriverRevenue = settlementDriverRevenue,
            success = true
        )

        fun failure(
            driverId: UUID,
            fullName: String? = null
        ) = DriverReportResult(
            driverId = driverId,
            fullName = fullName,
            success = false
        )
    }
}
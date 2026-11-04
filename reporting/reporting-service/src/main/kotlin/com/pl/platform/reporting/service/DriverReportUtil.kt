package com.pl.platform.reporting.service

import com.pl.platform.reporting.common.CurrencyTotalCalculator
import com.pl.platform.reporting.model.DriverReportResult
import com.pl.platform.svc.delivery.client.model.DeliveryItemResponse
import com.pl.platform.svc.delivery.client.model.DriverResponse
import com.pl.platform.svc.settlement.client.model.SettlementResponse
import java.util.UUID

object DriverReportUtil {
    fun buildReport(
        driverId: UUID,
        driver: DriverResponse,
        deliveries: List<DeliveryItemResponse>,
        settlements: List<SettlementResponse>
    ): DriverReportResult =
        DriverReportResult(
            driverId = driverId,
            fullName = "${driver.firstName} ${driver.lastName}",
            deliveriesCount = deliveries.size,
            deliveriesTotalCost = CurrencyTotalCalculator.sumByCurrency(
                deliveries,
                amount = { it.price },
                currency = { it.currency }
            ),
            settlementsCount = settlements.size,
            settlementDriverRevenue = CurrencyTotalCalculator.sumByCurrency(
                settlements,
                amount = { it.driverAmount },
                currency = { it.currency }
            )
        )

    fun threadInfo(): String {
        val thread = Thread.currentThread()

        return "threadId=${thread.threadId()}, virtual=${thread.isVirtual}"
    }

}
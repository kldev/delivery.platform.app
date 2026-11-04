package com.pl.platform.reporting.service

import com.pl.platform.reporting.common.CurrencyTotalCalculator
import com.pl.platform.reporting.model.DriverReport
import com.pl.platform.reporting.model.DriverReportResult
import com.pl.platform.reporting.service.DriverReportUtil.threadInfo
import com.pl.platform.svc.delivery.ReactiveDriverDeliveryService
import com.pl.platform.svc.settlement.ReactiveDriverSettlementService
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jboss.logging.Logger
import java.util.*


@ApplicationScoped
class CoroutineDriverReportingService(
    private val driverDeliveryService: ReactiveDriverDeliveryService,
    private val driverSettlementService: ReactiveDriverSettlementService
) {

    private val log = Logger.getLogger(DriverReportingService::class.java)

    @Throws(InterruptedException::class)
    suspend fun buildReport(driverIds: List<UUID>): DriverReport {
       return coroutineScope {

            log.debugf(
                "coroutineScope buildReport START: drivers=%d, thread=%s",
                driverIds.size,
                threadInfo()
            )
            val results = driverIds.map { driverId ->
                    fetchDriverReport(driverId)
            }


            val totalSettlement = CurrencyTotalCalculator.sumByCurrency(
                items = results
                    .flatMap { it.settlementDriverRevenue },
                amount = { it.total },
                currency = { it.currency }
            )

            val failureCount = results.count { !it.success }
            val successCount = results.count { it.success }

            log.debugf(
                "coroutineScope buildReport END: results=%d, failures=%d, thread=%s",
                results.size,
                failureCount,
                threadInfo()
            )

            DriverReport(
                failureCount = failureCount,
                results = results.toList(),
                totalSettlement = totalSettlement,
                successCount = successCount,
            )
        }
    }

    @Throws(InterruptedException::class)
    private suspend fun fetchDriverReport(driverId: UUID): DriverReportResult {
        return try {
            log.debugf(
                "coroutineScope fetchDriverReport START: driverId=%s, thread=%s",
                driverId,
                threadInfo()
            )

            coroutineScope {
                val deliveries = async {
                    driverDeliveryService.getDeliveries(driverId).awaitSuspending()
                }

                val settlements = async {
                    driverSettlementService.getSettlements(driverId).awaitSuspending()
                }

                val driver = async {
                    driverDeliveryService.getDriver(driverId).awaitSuspending()
                }

                DriverReportUtil.buildReport(
                    driverId = driverId,
                    driver = driver.await(),
                    deliveries = deliveries.await(),
                    settlements = settlements.await(),
                )
            }

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (e: Exception) {
            log.errorf(
                e,
                "coroutineScope Driver report FAILED: driverId=%s",
                driverId
            )

            DriverReportResult.failure(driverId)
        }
    }
}
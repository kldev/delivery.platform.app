package com.pl.platform.reporting.service

import com.pl.platform.reporting.common.CurrencyTotalCalculator
import com.pl.platform.reporting.model.DriverReport
import com.pl.platform.reporting.model.DriverReportResult
import com.pl.platform.reporting.service.DriverReportUtil.buildReport
import com.pl.platform.reporting.service.DriverReportUtil.threadInfo
import com.pl.platform.svc.delivery.DriverDeliveryService
import com.pl.platform.svc.delivery.client.model.DeliveryItemResponse
import com.pl.platform.svc.delivery.client.model.DriverResponse
import com.pl.platform.svc.settlement.DriverSettlementService
import com.pl.platform.svc.settlement.client.model.SettlementResponse
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.Joiner
import java.util.function.Consumer

@ApplicationScoped
class DriverReportingService(
    private val driverDeliveryService: DriverDeliveryService,
    private val driverSettlementService: DriverSettlementService
) {

    private val log = Logger.getLogger(DriverReportingService::class.java)

    @Throws(InterruptedException::class)
    fun buildReport(driverIds: List<UUID>): DriverReport {
        log.debugf(
            "buildReport START: drivers=%d, thread=%s",
            driverIds.size,
            threadInfo()
        )

       val results = driverIds.map {  driverId ->
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
            "buildReport END: results=%d, failures=%d, thread=%s",
            results.size,
            failureCount,
            threadInfo()
        )

        return DriverReport(
            failureCount = failureCount,
            results = results.toList(),
            totalSettlement = totalSettlement,
            successCount = successCount,
        )
    }

    @Throws(InterruptedException::class)
    private fun fetchDriverReport(driverId: UUID): DriverReportResult {
        return try {
            log.debugf(
                "fetchDriverReport START: driverId=%s, thread=%s",
                driverId,
                threadInfo()
            )

            StructuredTaskScope.open<Any?, Void?>(Joiner.awaitAll()).use { scope ->

                val deliveriesTask = scope.fork<List<DeliveryItemResponse>>(
                    Callable {
                        driverDeliveryService.getDeliveries(driverId)
                    }
                )

                val settlementTask = scope.fork<List<SettlementResponse>>(
                    Callable {
                        driverSettlementService.getSettlements(driverId)
                    }
                )

                val driverTask = scope.fork<DriverResponse>(
                    Callable {
                        driverDeliveryService.getDriver(driverId)
                    }
                )

                log.debugf(
                    "fetchDriverReport waiting for child tasks: driverId=%s, thread=%s",
                    driverId,
                    threadInfo()
                )

                scope.join()

                log.debugf(
                    "fetchDriverReport child tasks completed: driverId=%s, thread=%s",
                    driverId,
                    threadInfo()
                )

                return buildReport(
                    driverId = driverId,
                    driver = getResult(driverTask),
                    deliveries = getResult(deliveriesTask),
                    settlements = getResult(settlementTask)
                )
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (e: Exception) {
            log.errorf(
                e,
                "Driver report FAILED: driverId=%s",
                driverId
            )

            DriverReportResult.failure(driverId)
        }
    }

    private fun <T> getResult(
        task: StructuredTaskScope.Subtask<T>
    ): T {
        return when (task.state()) {
            StructuredTaskScope.Subtask.State.SUCCESS -> task.get()
            StructuredTaskScope.Subtask.State.FAILED -> throw task.exception()
            else -> error("Subtask did not complete")
        }
    }
}
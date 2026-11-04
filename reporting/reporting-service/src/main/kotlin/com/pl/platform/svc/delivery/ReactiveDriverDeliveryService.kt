package com.pl.platform.svc.delivery

import com.pl.platform.reporting.common.ReactiveHandleTooManyRequests.handleTooManyRequestsExceptionUni
import com.pl.platform.reporting.service.DriverReportUtil.threadInfo

import com.pl.platform.svc.common.SliceResponse
import com.pl.platform.svc.common.TooManyRequestsException
import com.pl.platform.svc.delivery.client.ReactiveDeliveryClient
import com.pl.platform.svc.delivery.client.model.DeliveryItemResponse
import com.pl.platform.svc.delivery.client.model.DeliveryStatus
import com.pl.platform.svc.delivery.client.model.DriverResponse
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.UUID

@ApplicationScoped
class ReactiveDriverDeliveryService(
    @RestClient
    private val client: ReactiveDeliveryClient
) {

    fun getDriversIds(): Uni<List<UUID>> =
        client.getDrivers(null)
            .onItem()
            .transform { drivers ->
                drivers.mapNotNull { it.id }
            }

    fun getDriver(driverId: UUID): Uni<DriverResponse> =
        getDriverWithRetry(driverId)
            .invoke { driver ->
                Log.debugf(
                    "getDriver: driverId=%s, driver=%s, thread=%s",
                    driverId,
                    driver,
                    threadInfo()
                )
            }

    fun getDeliveries(
        driverId: UUID
    ): Uni<List<DeliveryItemResponse>> =
        getDeliveriesPage(driverId, 0)
            .onItem()
            .transform { pages ->
                pages.flatMap { it.content }
            }

    private fun getDeliveriesPage(
        driverId: UUID,
        page: Int
    ): Uni<List<SliceResponse<DeliveryItemResponse>>> =
        getDeliveriesWithRetry(driverId, page)
            .chain { result ->

                if (!result.hasNext) {
                    Uni.createFrom().item(listOf(result))
                } else {
                    getDeliveriesPage(driverId, page + 1)
                        .map { nextPages ->
                            buildList {
                                add(result)
                                addAll(nextPages)
                            }
                        }
                }
            }

    private fun getDeliveriesWithRetry(
        driverId: UUID,
        page: Int,
        attempt: Int = 0
    ): Uni<SliceResponse<DeliveryItemResponse>> =
        client.getDeliveries(
            DeliveryStatus.DELIVERED,
            driverId,
            page
        ).onFailure(TooManyRequestsException::class.java)
            .recoverWithUni { exception ->
                handleTooManyRequestsExceptionUni(
                    exception = exception,
                    retryCount = attempt,
                    action = {
                        getDeliveriesWithRetry(
                            driverId = driverId,
                            page = page,
                            attempt = attempt + 1
                        )
                    },
                    identifier = driverId,
                    operation = "getDeliveries"
                )
            }

    private fun getDriverWithRetry(
        driverId: UUID,
        attempt: Int = 0
    ): Uni<DriverResponse> =
        client.getSingleDrivers(driverId)
            .onFailure(TooManyRequestsException::class.java)
            .recoverWithUni { exception ->
                handleTooManyRequestsExceptionUni(
                    exception = exception,
                    retryCount = attempt,
                    action = {
                        getDriverWithRetry(
                            driverId = driverId,
                            attempt = attempt + 1
                        )
                    },
                    identifier = driverId,
                    operation = "getSingleDrivers"
                )
            }
}
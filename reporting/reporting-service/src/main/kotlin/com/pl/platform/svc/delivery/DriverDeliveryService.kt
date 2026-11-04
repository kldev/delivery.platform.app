package com.pl.platform.svc.delivery

import com.pl.platform.svc.common.HandleTooManyRequests.handleTooManyRequestsException
import com.pl.platform.svc.common.SliceResponse
import com.pl.platform.svc.common.TooManyRequestsException
import com.pl.platform.svc.delivery.client.DeliveryClient
import com.pl.platform.svc.delivery.client.model.DeliveryItemResponse
import com.pl.platform.svc.delivery.client.model.DeliveryStatus
import com.pl.platform.svc.delivery.client.model.DriverResponse
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*

@ApplicationScoped
class DriverDeliveryService(
    @RestClient
    private val client: DeliveryClient
) {

    fun getDriversIds(): List<UUID> {
        return client.getDrivers(null).map { it.id }
    }

    fun getDriver(driverId: UUID): DriverResponse {
        val driver = getDriverWithRetry(driverId)

        Log.debugf(
            "getDriver: driverId=%s, driver=%s, thread=%s",
            driverId,
            driver,
            threadInfo()
        )

        return driver
    }

    fun getDeliveries(driverId: UUID): List<DeliveryItemResponse> {
        var page = 0
        val deliveries = mutableListOf<DeliveryItemResponse>()
        while (true) {
            val result = getDeliveriesWithRetry(driverId, page, 0)

            deliveries.addAll(result.content)

            if (!result.hasNext)
                break;
            page++

        }

        return deliveries
    }

    private fun getDeliveriesWithRetry(
        driverId: UUID,
        page: Int = 0,
        attempt: Int = 0,
    ): SliceResponse<DeliveryItemResponse> = try {
        client.getDeliveries(DeliveryStatus.DELIVERED, driverId, page)
    } catch (exception: TooManyRequestsException) {
        handleTooManyRequestsException(exception, attempt, {
            getDeliveriesWithRetry(driverId, page, attempt + 1)
        }, driverId, "getDeliveries")
    }

    private fun getDriverWithRetry(driverId: UUID, attempt: Int = 0): DriverResponse = try {
        client.getSingleDrivers(driverId)
    } catch (exception: TooManyRequestsException) {
        handleTooManyRequestsException(exception, attempt, {
            getDriverWithRetry(driverId, attempt + 1)
        }, driverId, "getSingleDrivers")
    }

    private fun threadInfo(): String {
        val thread = Thread.currentThread()

        return "threadId=${thread.threadId()}, virtual=${thread.isVirtual}"
    }

}
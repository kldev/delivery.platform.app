package com.pl.platform.reporting

import com.pl.platform.reporting.model.DriverReportRequest
import com.pl.platform.reporting.service.CoroutineDriverReportingService
import com.pl.platform.svc.delivery.ReactiveDriverDeliveryService
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path

@Path("/api/coroutine/driver")
class CoroutineDriverReportingResource(
    private val service: CoroutineDriverReportingService,
    private val driverDeliveryService: ReactiveDriverDeliveryService
) {

    @POST
    suspend fun getReport(request: DriverReportRequest) =
        service.buildReport(request.ids)

    @GET
    suspend fun getAllReport() = service.buildReport(driverDeliveryService.getDriversIds().awaitSuspending())
}
package com.pl.platform.reporting

import com.pl.platform.reporting.model.DriverReportRequest
import com.pl.platform.reporting.service.DriverReportingService
import com.pl.platform.svc.delivery.DriverDeliveryService
import com.pl.platform.svc.settlement.DriverSettlementService
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path

@Path("/api/reporting/driver")
class DriverReportingResource(
    private val service: DriverReportingService,
    private val driverDeliveryService: DriverDeliveryService
) {

    @POST
    fun getReport(request: DriverReportRequest) =
        service.buildReport(request.ids)

    @GET
    fun getAllReport() = service.buildReport(driverDeliveryService.getDriversIds())
}
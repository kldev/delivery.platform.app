package com.pl.platform

import com.pl.platform.svc.delivery.DriverDeliveryService
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import java.util.UUID


@Path("/api/driver")
class DriverResource(
    private val driverDeliveryService: DriverDeliveryService
) {

    @GET
    @Path("/{driverId}")
    fun getDeliveries(@PathParam("driverId") driverId: UUID)
        = driverDeliveryService.getDeliveries(driverId)
}
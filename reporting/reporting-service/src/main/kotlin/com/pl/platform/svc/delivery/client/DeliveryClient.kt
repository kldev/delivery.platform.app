package com.pl.platform.svc.delivery.client

import com.pl.platform.svc.common.PlatformResponseExceptionMapper
import com.pl.platform.svc.common.RestClientResponseLoggingFilter
import com.pl.platform.svc.common.SliceResponse
import com.pl.platform.svc.delivery.client.model.*
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.util.UUID

@RegisterRestClient(configKey = "delivery-client")
@RegisterProvider(PlatformResponseExceptionMapper::class)
@RegisterProvider(RestClientResponseLoggingFilter::class)
@Path("/api")
interface DeliveryClient {
    @GET
    @Path("/deliveries")
    fun getDeliveries(
        @QueryParam("status")
        status: DeliveryStatus?,

        @QueryParam("driverId")
        driverId: UUID?,

        @QueryParam("page")
        page: Int,
    ): SliceResponse<DeliveryItemResponse>

    @GET
    @Path("/drivers")
    fun getDrivers(
        @QueryParam("driverId")
        driverId: UUID?
    ): List<DriverResponse>

    @GET
    @Path("/drivers/{driverId}")
    fun getSingleDrivers(
        @PathParam("driverId")
        driverId: UUID
    ): DriverResponse
}
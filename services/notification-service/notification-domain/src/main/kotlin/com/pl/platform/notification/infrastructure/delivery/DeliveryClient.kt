package com.pl.platform.notification.infrastructure.delivery

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.util.UUID

/** delivery-service API. X-Api-Key (DeliveryApiKeyHeadersFactory) exempts these calls from its rate limit. */
@RegisterRestClient(configKey = "delivery-client")
@RegisterClientHeaders(DeliveryApiKeyHeadersFactory::class)
@RegisterProvider(DeliveryResponseExceptionMapper::class)
@Path("/api")
interface DeliveryClient {
    @GET
    @Path("/deliveries/{deliveryId}")
    fun getDelivery(
        @PathParam("deliveryId") deliveryId: UUID,
    ): Uni<DeliveryResponse>

    @GET
    @Path("/drivers/{driverId}")
    fun getDriver(
        @PathParam("driverId") driverId: UUID,
    ): Uni<DriverResponse>
}

/** Only the fields this service reads; others are ignored. */
data class DeliveryResponse(
    val id: UUID,
    val driverId: UUID?,
)

data class DriverResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
)

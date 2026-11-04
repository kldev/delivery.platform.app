package com.pl.platform.svc.settlement.client

import com.pl.platform.svc.common.PlatformResponseExceptionMapper
import com.pl.platform.svc.common.SliceResponse
import com.pl.platform.svc.settlement.client.model.SettlementResponse
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.util.UUID


@RegisterRestClient(configKey = "settlement-client-reactive")
@RegisterProvider(PlatformResponseExceptionMapper::class)
@Path("/api")
interface ReactiveSettlementClient {
    @GET
    @Path("/settlements")
    fun getSettlements(
        @QueryParam("search")
        search: String?,

        @QueryParam("page")
        page: Int,

        @QueryParam("driverId")
        driverId: UUID?,

        @QueryParam("deliveryId")
        deliveryId: UUID?

    ): Uni<SliceResponse<SettlementResponse>>
}
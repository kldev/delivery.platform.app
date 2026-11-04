package com.pl.platform

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import org.eclipse.microprofile.openapi.annotations.Operation

@Path("/")
class RootResource {

    @Operation(hidden = true)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Reporting Service"

    @Operation(hidden = true)
    @GET
    @Path("/docs")
    fun docs(): Response = Response.temporaryRedirect(
        URI("/q/swagger-ui")).build()
}
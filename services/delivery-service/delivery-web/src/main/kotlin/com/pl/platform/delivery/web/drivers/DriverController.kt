package com.pl.platform.delivery.web.drivers

import com.pl.platform.delivery.application.createdriver.CreateDriverHandler
import com.pl.platform.delivery.application.getdriver.GetDriverHandler
import com.pl.platform.delivery.application.getdriver.GetDriverQuery
import com.pl.platform.delivery.application.listdrivers.ListDriversHandler
import com.pl.platform.delivery.application.listdrivers.ListDriversQuery
import com.pl.platform.delivery.domain.DriverId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/** Errors are application/problem+json: 400 `validation`, 404 `driver.not-found`, 409 `driver.*-taken`. */
@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers")
class DriverController(
    private val createDriverHandler: CreateDriverHandler,
    private val listDriversHandler: ListDriversHandler,
    private val getDriverHandler: GetDriverHandler,
) {
    @PostMapping
    @Operation(
        summary = "Create a driver",
        description = """Registers an active driver. Phone number and email must be unique
(409 `driver.phone-number-taken` / `driver.email-taken`).""",
    )
    @ApiResponse(responseCode = "201", description = "Created; Location points to the driver")
    fun createDriver(
        @Valid @RequestBody request: CreateDriverRequest,
    ): ResponseEntity<DriverResponse> {
        val driver = createDriverHandler.handle(request.toCommand())
        return ResponseEntity
            .created(URI.create("/api/drivers/${driver.id.value}"))
            .body(DriverResponse.from(driver))
    }

    // Plain list, not a slice: the driver table is small and reporting/notification read it as an array.
    @GetMapping
    @Operation(summary = "List drivers")
    fun listDrivers(
        @Parameter(description = "Only this driver (kept for existing clients; prefer GET /api/drivers/{driverId})")
        @RequestParam(required = false) driverId: UUID?,
    ): List<DriverResponse> =
        listDriversHandler
            .handle(ListDriversQuery(driverId = driverId?.let(::DriverId)))
            .map(DriverResponse::from)

    @GetMapping("/{driverId}")
    @Operation(summary = "Get a driver")
    fun getDriver(
        @PathVariable driverId: UUID,
    ): DriverResponse = DriverResponse.from(getDriverHandler.handle(GetDriverQuery(DriverId(driverId))))
}

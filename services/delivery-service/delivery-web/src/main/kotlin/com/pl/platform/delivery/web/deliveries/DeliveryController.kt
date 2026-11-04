package com.pl.platform.delivery.web.deliveries

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.delivery.application.assigndriver.AssignDriverHandler
import com.pl.platform.delivery.application.cancel.CancelDeliveryCommand
import com.pl.platform.delivery.application.cancel.CancelDeliveryHandler
import com.pl.platform.delivery.application.complete.CompleteDeliveryCommand
import com.pl.platform.delivery.application.complete.CompleteDeliveryHandler
import com.pl.platform.delivery.application.confirm.ConfirmDeliveryCommand
import com.pl.platform.delivery.application.confirm.ConfirmDeliveryHandler
import com.pl.platform.delivery.application.create.CreateDeliveryHandler
import com.pl.platform.delivery.application.getdelivery.GetDeliveryHandler
import com.pl.platform.delivery.application.getdelivery.GetDeliveryQuery
import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesHandler
import com.pl.platform.delivery.application.listdeliveries.ListDeliveriesQuery
import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsHandler
import com.pl.platform.delivery.application.liststatusdurations.ListStatusDurationsQuery
import com.pl.platform.delivery.application.pickup.PickupDeliveryCommand
import com.pl.platform.delivery.application.pickup.PickupDeliveryHandler
import com.pl.platform.delivery.application.port.PageQuery
import com.pl.platform.delivery.application.start.StartDeliveryCommand
import com.pl.platform.delivery.application.start.StartDeliveryHandler
import com.pl.platform.delivery.domain.DeliveryId
import com.pl.platform.delivery.web.shared.idempotency.IdempotentExecutor
import com.pl.platform.delivery.web.shared.idempotency.execute
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * Errors are application/problem+json with a `code`: 400 `validation` (with `errors[]`), 404 `delivery.not-found`,
 * 409 `delivery.invalid-transition`, 422 `driver.none-available`, 429 `rate-limited`.
 */
@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Deliveries")
class DeliveryController(
    private val idempotent: IdempotentExecutor,
    private val createDeliveryHandler: CreateDeliveryHandler,
    private val getDeliveryHandler: GetDeliveryHandler,
    private val listDeliveriesHandler: ListDeliveriesHandler,
    private val listStatusDurationsHandler: ListStatusDurationsHandler,
    private val confirmDeliveryHandler: ConfirmDeliveryHandler,
    private val assignDriverHandler: AssignDriverHandler,
    private val pickupDeliveryHandler: PickupDeliveryHandler,
    private val startDeliveryHandler: StartDeliveryHandler,
    private val completeDeliveryHandler: CompleteDeliveryHandler,
    private val cancelDeliveryHandler: CancelDeliveryHandler,
) {
    @PostMapping
    @Operation(
        summary = "Create a delivery",
        description = """Creates a delivery in status CREATED and prices it by distance.

Requires the Idempotency-Key header (UUID). Repeating the request with the same key and body returns the
first response (with header Idempotent-Replayed: true) without creating another delivery. The same key with a
different body is rejected with 422 `idempotency.key-reused`; while the first request is still running, 409
`idempotency.in-progress`. A request that failed can be retried with the same key.""",
    )
    @ApiResponse(responseCode = "201", description = "Created; Location points to the delivery")
    fun createDelivery(
        @Parameter(description = "Unique key of this request (UUID)")
        @RequestHeader(IdempotentExecutor.HEADER) idempotencyKey: UUID,
        @Valid @RequestBody request: CreateDeliveryRequest,
    ): ResponseEntity<CreateDeliveryResponse> =
        idempotent.execute(scope = "POST /api/deliveries", key = idempotencyKey, request = request) {
            val result = createDeliveryHandler.handle(request.toCommand())
            ResponseEntity
                .created(URI.create("/api/deliveries/${result.deliveryId.value}"))
                .body(CreateDeliveryResponse.from(result))
        }

    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get a delivery")
    fun getDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryResponse = DeliveryResponse.from(getDeliveryHandler.handle(GetDeliveryQuery(DeliveryId(deliveryId))))

    @GetMapping
    @Operation(
        summary = "List deliveries",
        description = """Newest first. Returned as a slice: `hasNext` says whether another page exists,
without counting all rows.""",
    )
    fun listDeliveries(
        @Parameter(description = "Only deliveries in this status")
        @RequestParam(required = false) status: DeliveryStatusDto?,
        @Parameter(description = "Only this delivery")
        @RequestParam(required = false) deliveryId: UUID?,
        @Parameter(description = "Only deliveries of this driver")
        @RequestParam(required = false) driverId: UUID?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<DeliveryResponse> {
        val slice =
            listDeliveriesHandler.handle(
                ListDeliveriesQuery(
                    status = status?.toDomain(),
                    deliveryId = deliveryId,
                    driverId = driverId,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(DeliveryResponse::from), hasNext = slice.hasNext)
    }

    @GetMapping("/status-durations")
    @Operation(
        summary = "List status durations",
        description = """How long each delivery stayed in each status, oldest first. The current status has no
`statusTo`; its duration is counted up to now.""",
    )
    fun listStatusDurations(
        @Parameter(description = "Only this delivery")
        @RequestParam(required = false) deliveryId: UUID?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<DeliveryStatusDurationResponse> {
        val slice =
            listStatusDurationsHandler.handle(
                ListStatusDurationsQuery(deliveryId = deliveryId, page = PageQuery(page = page, size = size)),
            )
        return SliceResponse(
            content = slice.content.map(DeliveryStatusDurationResponse::from),
            hasNext = slice.hasNext,
        )
    }

    @PostMapping("/{deliveryId}/confirm")
    @Operation(summary = "Confirm a delivery", description = "CREATED -> CONFIRMED.")
    fun confirmDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(confirmDeliveryHandler.handle(ConfirmDeliveryCommand(DeliveryId(deliveryId))))

    @PostMapping("/{deliveryId}/assign-driver")
    @Operation(
        summary = "Assign a driver",
        description = """CONFIRMED -> ASSIGNED. Without `driverId` a random driver is picked; 422
`driver.none-available` when there is none, 404 `driver.not-found` for an unknown driverId.""",
    )
    fun assignDriver(
        @PathVariable deliveryId: UUID,
        @Valid @RequestBody(required = false) request: AssignDriverRequest?,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(
            assignDriverHandler.handle((request ?: AssignDriverRequest()).toCommand(deliveryId)),
        )

    @PostMapping("/{deliveryId}/pickup")
    @Operation(summary = "Pick up a delivery", description = "ASSIGNED -> PICKED_UP.")
    fun pickupDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(pickupDeliveryHandler.handle(PickupDeliveryCommand(DeliveryId(deliveryId))))

    @PostMapping("/{deliveryId}/start")
    @Operation(summary = "Start transit", description = "PICKED_UP -> IN_TRANSIT.")
    fun startDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(startDeliveryHandler.handle(StartDeliveryCommand(DeliveryId(deliveryId))))

    @PostMapping("/{deliveryId}/complete")
    @Operation(summary = "Complete a delivery", description = "IN_TRANSIT -> DELIVERED.")
    fun completeDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(completeDeliveryHandler.handle(CompleteDeliveryCommand(DeliveryId(deliveryId))))

    @PostMapping("/{deliveryId}/cancel")
    @Operation(
        summary = "Cancel a delivery",
        description = "From CREATED, ASSIGNED, PICKED_UP or IN_TRANSIT -> CANCELLED.",
    )
    fun cancelDelivery(
        @PathVariable deliveryId: UUID,
    ): DeliveryStateResponse =
        DeliveryStateResponse.from(cancelDeliveryHandler.handle(CancelDeliveryCommand(DeliveryId(deliveryId))))

    private companion object {
        const val MAX_PAGE_SIZE = 500L
    }
}

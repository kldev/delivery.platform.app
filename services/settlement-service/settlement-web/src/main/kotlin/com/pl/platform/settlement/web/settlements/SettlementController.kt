package com.pl.platform.settlement.web.settlements

import com.pl.platform.common.rest.SliceResponse
import com.pl.platform.settlement.application.getsettlement.GetSettlementHandler
import com.pl.platform.settlement.application.getsettlement.GetSettlementQuery
import com.pl.platform.settlement.application.listsettlements.ListSettlementsHandler
import com.pl.platform.settlement.application.listsettlements.ListSettlementsQuery
import com.pl.platform.settlement.application.port.PageQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/settlements")
@Tag(name = "Settlements")
class SettlementController(
    private val listSettlementsHandler: ListSettlementsHandler,
    private val getSettlementHandler: GetSettlementHandler,
) {
    @GetMapping
    @Operation(summary = "List settlements", description = "Newest first.")
    fun listSettlements(
        @Parameter(description = "Part of the driver's name, case-insensitive")
        @RequestParam(required = false) @Size(max = 200) search: String?,
        @RequestParam(required = false) driverId: UUID?,
        @RequestParam(required = false) deliveryId: UUID?,
        @Parameter(description = "Zero-based page number")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Page size, 1 to $MAX_PAGE_SIZE")
        @RequestParam(defaultValue = "100") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): SliceResponse<SettlementResponse> {
        val slice =
            listSettlementsHandler.handle(
                ListSettlementsQuery(
                    search = search,
                    driverId = driverId,
                    deliveryId = deliveryId,
                    page = PageQuery(page = page, size = size),
                ),
            )
        return SliceResponse(content = slice.content.map(SettlementResponse::from), hasNext = slice.hasNext)
    }

    @GetMapping("/{settlementId}")
    @Operation(summary = "Get a settlement")
    fun getSettlement(
        @PathVariable settlementId: UUID,
    ): SettlementResponse = SettlementResponse.from(getSettlementHandler.handle(GetSettlementQuery(settlementId)))

    private companion object {
        const val MAX_PAGE_SIZE = 500L
    }
}

package com.pl.platform.delivery.web.deliveries

import com.pl.platform.delivery.application.create.CreateDeliveryCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

// Required fields are nullable: a missing field then fails validation with its name instead of a generic 400
// for unreadable JSON.
data class CreateDeliveryRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    @field:NotBlank
    @field:Size(max = 500)
    val pickupAddress: String?,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    @field:NotBlank
    @field:Size(max = 500)
    val deliveryAddress: String?,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "42.5")
    @field:NotNull
    @field:DecimalMin(value = "0", inclusive = false)
    @field:DecimalMax("15000")
    @field:Digits(integer = 5, fraction = 4)
    val distanceKm: BigDecimal?,
    @field:Schema(description = "ISO 4217 code, PLN when omitted", example = "PLN")
    @field:Pattern(regexp = "[A-Z]{3}")
    val currency: String? = null,
) {
    // Called after @Valid passed, so the required fields are present.
    fun toCommand(): CreateDeliveryCommand =
        CreateDeliveryCommand(
            pickupAddress = pickupAddress!!.trim(),
            deliveryAddress = deliveryAddress!!.trim(),
            distanceKm = distanceKm!!,
            currency = currency ?: "PLN",
        )
}

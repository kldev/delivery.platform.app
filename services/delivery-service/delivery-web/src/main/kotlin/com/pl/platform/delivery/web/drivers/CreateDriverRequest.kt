package com.pl.platform.delivery.web.drivers

import com.pl.platform.delivery.application.createdriver.CreateDriverCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// Limits follow the columns of the drivers table.
data class CreateDriverRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank
    @field:Size(max = 100)
    val firstName: String?,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank
    @field:Size(max = 100)
    val lastName: String?,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "+48123123123")
    @field:NotBlank
    @field:Pattern(regexp = "\\+?[0-9]{6,20}", message = "must be 6 to 20 digits, optionally prefixed with +")
    val phoneNumber: String?,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 320)
    @field:NotBlank
    @field:Email
    @field:Size(max = 320)
    val email: String?,
) {
    // Called after @Valid passed, so the fields are present.
    fun toCommand() = CreateDriverCommand(firstName!!, lastName!!, phoneNumber!!, email!!)
}

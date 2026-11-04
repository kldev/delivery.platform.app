package com.pl.platform.common.rest

data class ApiValidationError(
    val message: String,
    val errors: List<ValidationError>
) {
    data class ValidationError(
        val field: String,
        val message: String?
    )
}
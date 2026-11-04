package com.pl.platform.delivery.web.shared

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ProblemDetail

/** Documents the problem+json error responses (ProblemDetailsAdvice) on every operation that can return them. */
@Configuration
class OpenApiConfiguration {
    @Bean
    fun problemResponses(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            ModelConverters.getInstance().read(ProblemDetail::class.java).forEach { (name, schema) ->
                openApi.components.addSchemas(name, schema)
            }
            val problem =
                Content().addMediaType(
                    "application/problem+json",
                    MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ProblemDetail")),
                )
            openApi.paths.orEmpty().forEach { (path, item) ->
                item.readOperationsMap().forEach { (method, operation) ->
                    val codes = errorCodes(method.name, path, operation.operationId)
                    codes.forEach { (code, description) ->
                        operation.responses.addApiResponse(code, ApiResponse().description(description).content(problem))
                    }
                }
            }
        }

    private fun errorCodes(
        method: String,
        path: String,
        operationId: String?,
    ): Map<String, String> =
        buildMap {
            put("400", "Validation failed (`validation`, with `errors[]`) or malformed request")
            if (path.contains("{")) put("404", "Delivery or driver not found (`*.not-found`)")
            if (method == "POST" && path.contains("{deliveryId}/")) {
                put("409", "Not allowed in the current status (`delivery.invalid-transition`)")
            }
            when (operationId) {
                "assignDriver" -> put("422", "No driver to assign (`driver.none-available`)")
                "createDriver" -> put("409", "Phone number or email taken (`driver.phone-number-taken`, `driver.email-taken`)")
                "createDelivery" -> {
                    put("400", "Validation failed, or Idempotency-Key missing/invalid (`idempotency.key-missing`, `idempotency.key-invalid`)")
                    put("409", "A request with this Idempotency-Key is in progress (`idempotency.in-progress`)")
                    put("422", "Idempotency-Key used with a different body (`idempotency.key-reused`)")
                }
            }
            if (method == "GET") put("429", "Rate limit exceeded (`rate-limited`), see Retry-After")
            put("500", "Unexpected error (`internal-error`)")
        }
}

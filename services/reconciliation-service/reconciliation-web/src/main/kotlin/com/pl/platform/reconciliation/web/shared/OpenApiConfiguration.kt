package com.pl.platform.reconciliation.web.shared

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ProblemDetail

/** Documents the problem+json error responses (ProblemDetailsAdvice) on every operation. */
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
                item.readOperations().forEach { operation ->
                    operation.responses.addApiResponse(
                        "400",
                        ApiResponse().description("Validation failed (`validation`, with `errors[]`)").content(problem),
                    )
                    if (path.contains("{")) {
                        operation.responses.addApiResponse(
                            "404",
                            ApiResponse().description("Not found (`*.not-found`)").content(problem),
                        )
                    }
                    operation.responses.addApiResponse(
                        "500",
                        ApiResponse().description("Unexpected error (`internal-error`)").content(problem),
                    )
                }
            }
        }
}

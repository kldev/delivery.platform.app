package com.pl.platform.notification.web.shared

import com.fasterxml.jackson.core.JsonProcessingException
import com.pl.platform.notification.domain.DomainException
import com.pl.platform.notification.domain.ErrorKind
import io.quarkus.logging.Log
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper

/** RFC 9457 body, the same shape as the Spring services' ProblemDetail (`code`, `errors[]`). */
data class ProblemBody(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String?,
    val instance: String?,
    val code: String,
    val errors: List<FieldError>? = null,
) {
    data class FieldError(
        val field: String,
        val message: String?,
    )
}

/** Every error of the API as application/problem+json; never a stack trace. */
class ProblemMappers {
    @ServerExceptionMapper
    fun domain(
        ex: DomainException,
        uri: UriInfo,
    ): RestResponse<ProblemBody> = problem(ex.kind.toStatus(), ex.message, ex.code, uri)

    @ServerExceptionMapper
    fun invalidRequest(
        ex: InvalidRequestException,
        uri: UriInfo,
    ): RestResponse<ProblemBody> = validation(listOf(ProblemBody.FieldError(ex.field, ex.message)), uri)

    @ServerExceptionMapper
    fun constraintViolation(
        ex: ConstraintViolationException,
        uri: UriInfo,
    ): RestResponse<ProblemBody> =
        validation(
            ex.constraintViolations.map { violation ->
                // "createNotification.request.eventId" -> "eventId"; "listNotifications.size" -> "size"
                val path = violation.propertyPath.toString().split('.')
                ProblemBody.FieldError(path.drop(if (path.size > 2) 2 else 1).joinToString("."), violation.message)
            },
            uri,
        )

    @ServerExceptionMapper
    fun unreadableBody(
        ex: JsonProcessingException,
        uri: UriInfo,
    ): RestResponse<ProblemBody> = problem(Response.Status.BAD_REQUEST, "Malformed JSON request body", "bad-request", uri)

    /** 404 for an unknown path, 405, 415, ... keep their status, in the problem shape. */
    @ServerExceptionMapper
    fun web(
        ex: WebApplicationException,
        uri: UriInfo,
    ): RestResponse<ProblemBody> {
        val status = Response.Status.fromStatusCode(ex.response.status) ?: Response.Status.INTERNAL_SERVER_ERROR
        return problem(status, status.reasonPhrase, "http-${status.statusCode}", uri)
    }

    @ServerExceptionMapper
    fun unexpected(
        ex: Exception,
        uri: UriInfo,
    ): RestResponse<ProblemBody> {
        Log.error("Unhandled exception", ex)
        return problem(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", "internal-error", uri)
    }

    private fun validation(
        errors: List<ProblemBody.FieldError>,
        uri: UriInfo,
    ) = problem(Response.Status.BAD_REQUEST, "Request validation failed", "validation", uri, errors)

    private fun problem(
        status: Response.Status,
        detail: String?,
        code: String,
        uri: UriInfo,
        errors: List<ProblemBody.FieldError>? = null,
    ): RestResponse<ProblemBody> =
        RestResponse.ResponseBuilder
            .create(status, ProblemBody("urn:problem:$code", status.reasonPhrase, status.statusCode, detail, uri.path, code, errors))
            .type("application/problem+json")
            .build()
}

private fun ErrorKind.toStatus(): Response.Status =
    when (this) {
        ErrorKind.NOT_FOUND -> Response.Status.NOT_FOUND
        ErrorKind.CONFLICT -> Response.Status.CONFLICT
    }

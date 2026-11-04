package com.pl.platform.ledger.web.shared

import com.pl.platform.ledger.domain.DomainException
import com.pl.platform.ledger.domain.ErrorKind
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.beans.TypeMismatchException
import java.net.URI

/**
 * Every error of the API as RFC 9457 problem+json with a stable `code`.
 * Spring MVC errors (unreadable body, wrong parameter type, 405, 415, ...) come from ResponseEntityExceptionHandler.
 */
@RestControllerAdvice
class ProblemDetailsAdvice : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainException::class)
    fun domain(ex: DomainException): ProblemDetail =
        problem(ex.kind.toStatus(), ex.message ?: ex.code, ex.code)

    /** Cross-field checks done in the controller (e.g. from after to). */
    @ExceptionHandler(InvalidRequestException::class)
    fun invalidRequest(ex: InvalidRequestException): ProblemDetail =
        validationProblem(listOf(FieldError(ex.field, ex.message)))

    @ExceptionHandler(Exception::class)
    fun unexpected(ex: Exception): ProblemDetail {
        log.error("Unhandled exception", ex)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "internal-error")
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val body =
            validationProblem(
                ex.bindingResult.fieldErrors.map { FieldError(it.field, it.defaultMessage) },
            )
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request)
    }

    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errors =
            ex.parameterValidationResults.flatMap { result ->
                val name = result.methodParameter.parameterName ?: "parameter"
                result.resolvableErrors.map { FieldError(name, it.defaultMessage) }
            }
        return handleExceptionInternal(ex, validationProblem(errors), headers, HttpStatus.BAD_REQUEST, request)
    }

    override fun handleServletRequestBindingException(
        ex: ServletRequestBindingException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val body = problem(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request", "bad-request")
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request)
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val name = (ex as? MethodArgumentTypeMismatchException)?.name ?: ex.propertyName
        val body = validationProblem(listOf(FieldError(name ?: "parameter", "Invalid value")))
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request)
    }

    private fun validationProblem(errors: List<FieldError>): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Request validation failed", "validation").apply {
            setProperty("errors", errors)
        }

    private fun problem(
        status: HttpStatus,
        detail: String,
        code: String,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            type = URI.create("urn:problem:$code")
            title = status.reasonPhrase
            setProperty("code", code)
        }

    data class FieldError(
        val field: String,
        val message: String?,
    )
}

private fun ErrorKind.toStatus(): HttpStatus =
    when (this) {
        ErrorKind.NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorKind.CONFLICT -> HttpStatus.CONFLICT
        ErrorKind.RULE_VIOLATION -> HttpStatus.UNPROCESSABLE_CONTENT
    }

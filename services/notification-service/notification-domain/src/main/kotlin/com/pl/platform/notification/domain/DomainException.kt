package com.pl.platform.notification.domain

/** How a domain error maps to the outside world; the web layer turns it into an HTTP status. */
enum class ErrorKind {
    NOT_FOUND,
    CONFLICT,
}

/** Base of every expected business error; [code] is stable and machine-readable. */
abstract class DomainException(
    val kind: ErrorKind,
    val code: String,
    message: String,
) : RuntimeException(message)

class NotFoundException(
    entity: String,
    id: Any,
) : DomainException(ErrorKind.NOT_FOUND, "$entity.not-found", "$entity $id not found")

class ConflictException(
    code: String,
    message: String,
) : DomainException(ErrorKind.CONFLICT, code, message)

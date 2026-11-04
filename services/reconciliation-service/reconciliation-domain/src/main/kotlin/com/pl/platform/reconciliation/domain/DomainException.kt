package com.pl.platform.reconciliation.domain

/** How a domain error maps to the outside world; the web layer turns it into an HTTP status. */
enum class ErrorKind {
    NOT_FOUND,
    CONFLICT,
    RULE_VIOLATION,
}

/**
 * Base of every expected business error. [code] is stable and machine-readable ("delivery.invalid-transition"),
 * the message is for humans and carries no personal data.
 */
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

class RuleViolationException(
    code: String,
    message: String,
) : DomainException(ErrorKind.RULE_VIOLATION, code, message)

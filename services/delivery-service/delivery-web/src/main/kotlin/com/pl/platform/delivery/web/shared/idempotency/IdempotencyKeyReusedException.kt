package com.pl.platform.delivery.web.shared.idempotency

import com.pl.platform.delivery.domain.DomainException
import com.pl.platform.delivery.domain.ErrorKind

class IdempotencyKeyReusedException :
    DomainException(
        ErrorKind.RULE_VIOLATION,
        "idempotency.key-reused",
        "Idempotency-Key was already used with a different request body",
    )

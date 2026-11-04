package com.pl.platform.delivery.application.shared.idempotency

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class RequestHasher {

    fun hash(body: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")

        return digest
            .digest(body)
            .joinToString("") { "%02x".format(it) }
    }
}
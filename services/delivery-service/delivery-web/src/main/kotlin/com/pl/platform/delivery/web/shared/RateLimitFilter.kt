package com.pl.platform.delivery.web.shared

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty(
    name = ["rate.limit.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class RateLimitFilter(@Value($$"${rate.bucket.size:100}")
                            private val rateLimit: Long,
                            @Value($$"${rate.bucket.override.key}")
                          private val overrideKey:String) : OncePerRequestFilter() {
    var buckets: Cache<String, Bucket> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientKey = getClientIp(request) ?: "unknown-ip"
        val bucket = buckets.get(clientKey) { createBucket(it) }

        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            filterChain.doFilter(request, response)
        } else {
            val waitSeconds = Duration.ofNanos(probe.nanosToWaitForRefill)
                .seconds
                .let { if (it == 0L) 1L else it }
            response.status = 429
            response.setHeader("Retry-After", waitSeconds.toString())
            // Same problem+json shape as the rest of the API; the filter runs before Spring MVC, so it is written by hand.
            response.contentType = "application/problem+json"
            response.characterEncoding = "UTF-8"
            response.writer.write(
                """{"type":"urn:problem:rate-limited","title":"Too Many Requests","status":429,""" +
                    """"detail":"Too many requests, retry after $waitSeconds s","code":"rate-limited"}""",
            )
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val apiKey = request.getHeader("X-Api-Key") ?: ""
        return apiKey.isNotBlank() && apiKey.equals(overrideKey, ignoreCase = false)
                || !(request.method == "GET" && (request.requestURI.contains("/api/driver", true)
                || request.requestURI.contains("/api/deliveries", true)))
    }

    private fun getClientIp(request: HttpServletRequest): String? {
        // X-Forwarded-For can be spoofed by clients; only use it behind a trusted reverse proxy.
        // In directly exposed environments, using getRemoteAddr() only is safer.
        val forwarded = request.getHeader("X-Forwarded-For")
        if (forwarded != null && forwarded.isNotBlank()) {
            return forwarded.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
        }
        return request.remoteAddr
    }

    private fun createBucket(key: String): Bucket {
        val limit = Bandwidth.builder()
            .capacity(rateLimit)
            .refillGreedy(rateLimit, Duration.ofMinutes(1))
            .build()
        return Bucket.builder().addLimit(limit).build()
    }
}
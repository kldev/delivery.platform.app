package com.pl.platform.notification.application.port

/** Zero-based page of a list query. */
data class PageQuery(
    val page: Int = 0,
    val size: Int = 100,
)

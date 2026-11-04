package com.pl.platform.reconciliation.application.port

/** Zero-based page of a list query; sorting is fixed per query by its repository. */
data class PageQuery(
    val page: Int = 0,
    val size: Int = 100,
)

package com.pl.platform.svc.common

data class SliceResponse<T>(
    val content: List<T>,
    val hasNext: Boolean
)
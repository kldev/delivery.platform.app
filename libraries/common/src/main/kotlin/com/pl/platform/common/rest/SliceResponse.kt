package com.pl.platform.common.rest

data class SliceResponse<T>(
    val content: List<T>,
    val hasNext: Boolean
) 

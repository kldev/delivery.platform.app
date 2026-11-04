package com.pl.platform.reporting.model

@JvmRecord
data class DriverReport(
    val results: List<DriverReportResult>,
    val totalSettlement: List<TotalWithCurrency>,
    val successCount: Int,
    val failureCount: Int
) {
    val isPartial: Boolean
        get() = failureCount > 0 && successCount > 0

    val isSuccess: Boolean
        get() = failureCount == 0
}
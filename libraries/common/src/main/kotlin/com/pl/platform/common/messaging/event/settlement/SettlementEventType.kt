package com.pl.platform.common.messaging.event.settlement

enum class SettlementEventType(val value: String) {
    Created("settlement.created"),
    DriverSettlementCompleted("driver.settlement.completed"),
}
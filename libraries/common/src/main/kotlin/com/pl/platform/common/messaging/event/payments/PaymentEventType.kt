package com.pl.platform.common.messaging.event.payments

enum class PaymentEventType(val value: String) {
    Paid("payment.paid"),
    Declined("payment.declined"),
    Completed("payment.completed")
}
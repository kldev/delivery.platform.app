package com.pl.platform.notification.domain

/** Who a notification goes to; each channel needs its own field (email, SMS, push by name). */
data class NotificationRecipient(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
)

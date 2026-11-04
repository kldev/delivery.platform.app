package com.pl.platform.notification.web.notifications

import com.pl.platform.notification.domain.NotificationChannel
import com.pl.platform.notification.domain.NotificationStatus

// API enums, mapped explicitly: renaming a domain constant must not silently change the contract.

enum class NotificationChannelDto {
    EMAIL,
    SMS,
    PUSH,
    ;

    fun toDomain(): NotificationChannel =
        when (this) {
            EMAIL -> NotificationChannel.EMAIL
            SMS -> NotificationChannel.SMS
            PUSH -> NotificationChannel.PUSH
        }

    companion object {
        fun from(channel: NotificationChannel): NotificationChannelDto =
            when (channel) {
                NotificationChannel.EMAIL -> EMAIL
                NotificationChannel.SMS -> SMS
                NotificationChannel.PUSH -> PUSH
            }
    }
}

enum class NotificationStatusDto {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    ;

    fun toDomain(): NotificationStatus =
        when (this) {
            PENDING -> NotificationStatus.PENDING
            PROCESSING -> NotificationStatus.PROCESSING
            SENT -> NotificationStatus.SENT
            FAILED -> NotificationStatus.FAILED
        }

    companion object {
        fun from(status: NotificationStatus): NotificationStatusDto =
            when (status) {
                NotificationStatus.PENDING -> PENDING
                NotificationStatus.PROCESSING -> PROCESSING
                NotificationStatus.SENT -> SENT
                NotificationStatus.FAILED -> FAILED
            }
    }
}

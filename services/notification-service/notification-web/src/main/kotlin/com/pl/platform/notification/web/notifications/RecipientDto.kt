package com.pl.platform.notification.web.notifications

import com.pl.platform.notification.domain.NotificationRecipient
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/** Who to notify; each channel uses its own field (EMAIL email, SMS phoneNumber, PUSH fullName). */
data class RecipientDto(
    @field:Size(max = 200)
    val fullName: String? = null,
    @field:Pattern(regexp = "\\+?[0-9]{6,20}", message = "must be 6 to 20 digits, optionally prefixed with +")
    val phoneNumber: String? = null,
    @field:Email
    @field:Size(max = 320)
    val email: String? = null,
) {
    fun toDomain() = NotificationRecipient(fullName = fullName, phoneNumber = phoneNumber, email = email)

    companion object {
        fun from(recipient: NotificationRecipient) =
            RecipientDto(fullName = recipient.fullName, phoneNumber = recipient.phoneNumber, email = recipient.email)
    }
}

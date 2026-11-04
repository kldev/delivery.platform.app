package com.pl.platform.delivery.testing

import com.pl.platform.delivery.domain.Driver
import java.security.SecureRandom

class DriverTestFactory {
    companion object {
        private val random = SecureRandom()
        fun fakeEmail(
            firstName: String,
            lastName: String,
        ): String =
            "${firstName.trim().lowercase()}.${lastName.trim().lowercase()}${random.nextInt(9000)}@fake.io"

        fun create(
            firstName: String = "John",
            lastName: String = "Connor",
            phoneNumber: String = "+48" + random.nextInt(1000) + random.nextInt(1000) + random.nextInt(100),
        ): Driver =
            Driver.create(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                fakeEmail(firstName, lastName),

            )
    }
}
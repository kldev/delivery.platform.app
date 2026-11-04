package com.pl.platform.svc.delivery.client.model

import java.util.UUID

data class DriverResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
)
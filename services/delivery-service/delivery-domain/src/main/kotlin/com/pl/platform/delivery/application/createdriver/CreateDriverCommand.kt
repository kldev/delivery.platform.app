package com.pl.platform.delivery.application.createdriver

data class CreateDriverCommand(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
)

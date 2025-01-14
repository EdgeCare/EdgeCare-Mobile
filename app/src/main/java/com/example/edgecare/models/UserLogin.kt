package com.example.edgecare.models


data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val expiresAt: String,   // [TODO] - change to date and time
    val message: String
)
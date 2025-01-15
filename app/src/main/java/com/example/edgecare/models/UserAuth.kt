package com.example.edgecare.models

data class UserCreateRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val token: String,
    val userId: String,
    val expiresAt: String,   // [TODO] - change to date and time
    val message: String
)
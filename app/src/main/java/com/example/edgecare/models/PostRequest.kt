package com.example.edgecare.models

data class PostRequest(
    val userId: Int = 0,
    val id: Int = 1,
    val token: String = "token",
    val type: String,
    val body: String
)

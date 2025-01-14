package com.example.edgecare.models

data class UserQuestionRequest(
    val userId: Int = 0,
    val token: String = "TOKEN",
    val content: String
)

data class UserQuestionResponse(
    val content: String,
    val status: String
)
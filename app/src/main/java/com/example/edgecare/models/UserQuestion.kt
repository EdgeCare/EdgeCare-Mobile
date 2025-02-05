package com.example.edgecare.models

data class UserQuestionRequest(
    val userId: Int = 0,
    val chatId:Long ,
    val token: String = "TOKEN",
    val content: String,
    val healthReports:String,
)

data class UserQuestionResponse(
    val content: String,
    val status: String
)

data class UserPersona(
    val userId:Long,
    val details: String,
    val token: String = "TOKEN",
)
package com.example.edgecare.models

data class QuestionnaireQuestion(
    val id: Int,
    val questionText: String,
    val databaseColumn: String,
    val inputType: String,
    val explanationQuestion: String? = null,
    val explanationText: String? = null,
    val options: List<String>? = null,
    var answer: Any? = null     // String, Int, or Date as answer
)

package com.example.edgecare.models

data class Question(
    val questionText: String,
    val inputType: String,
    val explanationQuestion: String? = null,
    val explanationText: String? = null,
    val options: List<String>? = null
)

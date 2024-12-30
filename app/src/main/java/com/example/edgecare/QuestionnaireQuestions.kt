package com.example.edgecare

import com.example.edgecare.models.QuestionnaireQuestion

val questionsList = listOf(
    QuestionnaireQuestion(
        1,
        "What is your name?",
        "name",
        "text"
    ),
    QuestionnaireQuestion(
        2,
        "Hi ###, are you male or female?",
        "gender",
        "select",
        "Why only male and female?",
        "Here we consider only Male and Female",
        options = listOf("Male", "Female")
    ),
    QuestionnaireQuestion(
        3,
        "What is your date of birth?",
        "birthday",
        "text"
    ),
    QuestionnaireQuestion(
        4,
        "Are you a current smoker or have you been a smoker in the past?",
        "smoking",
        "select",
        "This question is required.",
        options = listOf("Yes", "No")
    ),
    QuestionnaireQuestion(
        5,
        "Have you ever been diagnosed with high blood pressure?",
        "highBloodPressure",
        "select",
        "This question is required.",
        options = listOf("Yes", "No")
    ),
    QuestionnaireQuestion(
        6,
        "Do you have diabetes?",
        "diabetes",
        "select",
        "This question is required.",
        options = listOf("Yes", "No")),
    QuestionnaireQuestion(
        7,
        "How many hours do you sleep?",
        "sleepHours",
        "number",
        "Provide an approximate number in hours"
    )
)
package com.example.edgecare.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.R
import com.example.edgecare.fragments.QuestionFragment
import com.example.edgecare.models.Question

class HostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        val questions = listOf(
            Question("What is your name?", "text", "Enter your full name."),
            Question("Hi ###, are you male or female?", "select", "This question is required."),
            Question("What is your date of birth?", "text", "Enter your birth day."),
            Question("Are you current smoker or have you been a smoker in the past?", "select", "This question is required."),
            Question("Have you ever been diagnosed with high blood pressure?", "select", "This question is required."),
            Question("Do you have diabetes?", "select", "This question is required."),
            Question("How many hours do you sleep?", "number", "Provide an approximate number.")
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, QuestionFragment.newInstance(questions, 0))
            .commit()
    }
}


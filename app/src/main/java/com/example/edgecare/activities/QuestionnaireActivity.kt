package com.example.edgecare.activities

import android.R.layout
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.databinding.ActivityQuestionnaireBinding
import com.example.edgecare.models.Question

class QuestionnaireActivity : AppCompatActivity() {

    private val questions = listOf(
        Question("What is your name?", "text"),
        Question(
            "Hi ###, are you male or female?",
            "select",
            "Why only male and female?",
            "Here we consider only Male and Female",
            listOf("Male", "Female")
        ),
        Question("What is your date of birth?", "text"),
        Question(
            "Are you a current smoker or have you been a smoker in the past?",
            "select",
            "This question is required.",
            options = listOf("Yes", "No")
        ),
        Question(
            "Have you ever been diagnosed with high blood pressure?",
            "select",
            "This question is required.",
            options = listOf("Yes", "No")
        ),
        Question(
            "Do you have diabetes?",
            "select",
            "This question is required.",
            "",
            listOf("Yes", "No")),
        Question(
            "How many hours do you sleep?",
            "number",
            "Provide an approximate number.")
    )

    private var currentQuestionIndex = 0
    private lateinit var binding: ActivityQuestionnaireBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Binding
        binding = ActivityQuestionnaireBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        // Load the first question
        loadQuestion(currentQuestionIndex)

        // Set button listeners
        binding.previousButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                loadQuestion(currentQuestionIndex)
            }
        }

        binding.nextButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
            } else {
                // Navigate to EndFragment [Todo]
//                val intent = Intent(this, QuestionEndFragment::class.java)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadQuestion(index: Int) {
        val question = questions[index]

        // Update previousButton visibility
        binding.previousButton.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE

        // Update the question text
        binding.questionText.text = question.questionText

        // Show or hide explanation button
        if (!question.explanationQuestion.isNullOrEmpty() && !question.explanationText.isNullOrEmpty()) {
            binding.explanationButton.visibility = View.VISIBLE
            binding.explanationButton.setOnClickListener {
                Toast.makeText(this, question.explanationText, Toast.LENGTH_LONG).show()
            }
        } else {
            binding.explanationButton.visibility = View.GONE
        }

        // Load input type dynamically
        loadInputType(question)
    }

    private fun loadInputType(question: Question) {
        binding.inputContainer.removeAllViews()

        when (question.inputType) {
            "text" -> {
                val editText = EditText(this).apply {
                    hint = "Enter your answer"
                }
                binding.inputContainer.addView(editText)
            }
            "select" -> {
                val spinner = Spinner(this)
                val adapter = ArrayAdapter(
                    this,
                    layout.simple_spinner_dropdown_item,
                    question.options ?: listOf("Yes", "No")
                )
                spinner.adapter = adapter
                binding.inputContainer.addView(spinner)
            }
            "number" -> {
                val numberInput = EditText(this).apply {
                    hint = "Enter a number"
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                }
                binding.inputContainer.addView(numberInput)
            }
            else -> {
                val unknownText = TextView(this).apply {
                    text = "Unsupported input type"
                }
                binding.inputContainer.addView(unknownText)
            }
        }
    }
}

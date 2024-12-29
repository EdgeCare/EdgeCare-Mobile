package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.R
import com.example.edgecare.databinding.ActivityQuestionnaireBinding
import com.example.edgecare.models.QuestionnaireQuestion
import com.example.edgecare.questionsList

class QuestionnaireActivity : AppCompatActivity() {

    private val questions = questionsList
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
                saveAnswer(currentQuestionIndex)
                currentQuestionIndex--
                loadQuestion(currentQuestionIndex)
            }
        }

        binding.nextButton.setOnClickListener {
            saveAnswer(currentQuestionIndex)

            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
            } else {
                saveAllAnswers()
            }
        }
    }

    private fun loadQuestion(index: Int) {
        val question = questions[index]

        // Update visibility of the "Previous" button
        binding.previousButton.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE

        // Update the question text
        binding.questionText.text = question.questionText

        // Show or hide the "Explanation" button
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

    private fun loadInputType(question: QuestionnaireQuestion) {
        binding.inputContainer.removeAllViews()

        when (question.inputType) {
            "text" -> {
                // Show "Next" button
                binding.nextButton.visibility = View.VISIBLE

                val textInputView = layoutInflater.inflate(R.layout.questionnaire_input_text, binding.inputContainer, false)
                binding.inputContainer.addView(textInputView)

                val editText = textInputView.findViewById<EditText>(R.id.questionnaireTextInput)
                editText.setText(question.answer as? String ?: "")
            }
            "select" -> {
                // Hide "Next" button
                binding.nextButton.visibility = View.GONE

                val selectInputView = layoutInflater.inflate(R.layout.questionnaire_input_select, binding.inputContainer, false)
                binding.inputContainer.addView(selectInputView)

                val buttons = listOf(
                    selectInputView.findViewById<Button>(R.id.optionButton1),
                    selectInputView.findViewById<Button>(R.id.optionButton2),
                    selectInputView.findViewById<Button>(R.id.optionButton3)
                )

                buttons.forEachIndexed { index, button ->
                    if (index < (question.options?.size ?: 0)) {
                        button.visibility = View.VISIBLE
                        button.text = question.options?.get(index) ?: ""
                        button.setOnClickListener {
                            // Save answer and navigate to the next question
                            question.answer = button.text.toString()
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                loadQuestion(currentQuestionIndex)
                            } else {
                                saveAllAnswers()
                            }
                        }
                    } else {
                        button.visibility = View.GONE
                    }
                }
            }
            "number" -> {
                // Show "Next" button
                binding.nextButton.visibility = View.VISIBLE

                val numberInputView = layoutInflater.inflate(R.layout.questionnaire_input_number, binding.inputContainer, false)
                binding.inputContainer.addView(numberInputView)

                val editText = numberInputView.findViewById<EditText>(R.id.questionnaireNumberInput)
                editText.setText((question.answer as? Int)?.toString() ?: "")
            }
            else -> {
                val unknownText = TextView(this).apply {
                    text = "Unsupported input type"
                }
                binding.inputContainer.addView(unknownText)
            }
        }
    }

    private fun saveAnswer(index: Int) {
        val question = questions[index]
        val inputContainer = binding.inputContainer

        when (question.inputType) {
            "text" -> {
                val editText = inputContainer.findViewById<EditText>(R.id.questionnaireTextInput)
                question.answer = editText?.text?.toString()
            }
            "number" -> {
                val editText = inputContainer.findViewById<EditText>(R.id.questionnaireNumberInput)
                question.answer = editText?.text?.toString()?.toIntOrNull()
            }
            "select" -> {
                // Do nothing here; answers are saved on button click
            }
        }
    }

    private fun saveAllAnswers() {
        questions.forEach {
            // [TODO] Save answers to the database
            println("Question: ${it.questionText}, Answer: ${it.answer}")
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

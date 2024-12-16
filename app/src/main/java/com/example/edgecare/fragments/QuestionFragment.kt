package com.example.edgecare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.edgecare.R
import com.example.edgecare.databinding.FragmentQuestionBinding
import com.example.edgecare.models.Question

class QuestionFragment : Fragment() {

    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    // Arguments for the question fragment
    companion object {
        private const val ARG_QUESTIONS = "questions"
        private const val ARG_INDEX = "index"

        fun newInstance(questions: List<Question>, index: Int): QuestionFragment {
            val fragment = QuestionFragment()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_QUESTIONS, ArrayList(questions))
                putInt(ARG_INDEX, index)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the list of questions and the current index
        val questions = arguments?.getParcelableArrayList<Question>(ARG_QUESTIONS)
        val currentIndex = arguments?.getInt(ARG_INDEX) ?: 0

        if (questions.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No questions available", Toast.LENGTH_SHORT).show()
            return
        }

        val currentQuestion = questions[currentIndex]

        // Set question text
        binding.questionText.text = currentQuestion.questionText

        // Load input type dynamically
        loadInputType(currentQuestion.inputType)

        // Set explanation button click listener
        binding.explanationButton.setOnClickListener {
            Toast.makeText(requireContext(), currentQuestion.explanation, Toast.LENGTH_LONG).show()
        }

        // Previous button functionality
        binding.previousButton.setOnClickListener {
            if (currentIndex > 0) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        newInstance(questions, currentIndex - 1)
                    )
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "No previous question", Toast.LENGTH_SHORT).show()
            }
        }

        // Next button functionality
        binding.nextButton.setOnClickListener {
            if (currentIndex < questions.size - 1) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        newInstance(questions, currentIndex + 1)
                    )
                    .addToBackStack(null)
                    .commit()
            } else {
                // Navigate to EndFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, QuestionEndFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadInputType(inputType: String) {
        when (inputType) {
            "text" -> {
                val textInput = layoutInflater.inflate(R.layout.text_input, binding.inputContainer, false)
                binding.inputContainer.addView(textInput)
            }
            "select" -> {
                val selectInput = layoutInflater.inflate(R.layout.select_input, binding.inputContainer, false)
                binding.inputContainer.addView(selectInput)
            }
            "number" -> {
                val numberInput = layoutInflater.inflate(R.layout.number_input, binding.inputContainer, false)
                binding.inputContainer.addView(numberInput)
            }
            // Add other input types as needed
            else -> {
                Toast.makeText(requireContext(), "Unknown input type: $inputType", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

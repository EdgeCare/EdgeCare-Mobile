package com.example.edgecare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.edgecare.BertModelHandler
import com.example.edgecare.EdgeCareApp
import com.example.edgecare.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainContentFragment : Fragment() {

    private lateinit var modelHandler: BertModelHandler
    private lateinit var outputTextView: TextView
    private lateinit var sendButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main_content, container, false)

        // Initialize UI components
        val inputEditText = view.findViewById<EditText>(R.id.mainVIewInputText)
        outputTextView = view.findViewById(R.id.mainVIewOutputText)
        sendButton = view.findViewById(R.id.sendButton)


        // Initialize BERT model handler
        modelHandler = (requireActivity().application as EdgeCareApp).modelHandler

        // Set up click listener for the send button
        sendButton.setOnClickListener {
            val inputText = inputEditText.text.toString()
            if (inputText.isNotEmpty()) {
                processInputText(inputText)
                inputEditText.text.clear() // Clear input after processing
            } else {
                Toast.makeText(requireContext(), "Input is empty", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun processInputText(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val features = modelHandler.prepareInputs(text)
            val result = modelHandler.runInference(features)
            displayResults(result)
        }
    }

    private fun displayResults(labeledTokens: List<Pair<String, String>>) {
        val resultBuilder = StringBuilder()
        labeledTokens.forEach { (token, label) ->
            resultBuilder.append("$token -> $label\n")
        }
        outputTextView.text = resultBuilder.toString()
    }
}

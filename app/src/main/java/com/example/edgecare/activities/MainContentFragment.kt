package com.example.edgecare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.BertModelHandler
import com.example.edgecare.EdgeCareApp
import com.example.edgecare.adapters.ChatAdapter
import com.example.edgecare.databinding.ActivityMainContentBinding
import com.example.edgecare.models.ChatMessage
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.SimilaritySearchUtils
import io.objectbox.Box
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainContentFragment : Fragment() {

    private var _binding: ActivityMainContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var modelHandler: BertModelHandler
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit  var healthReportBox: Box<HealthReport>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = ActivityMainContentBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize BERT model handler
        modelHandler = (requireActivity().application as EdgeCareApp).modelHandler

        // Set up click listener for the send button
        binding.sendButton.setOnClickListener {
            val inputText = binding.mainVIewInputText.text.toString()
            if (inputText.isNotEmpty()) {
                binding.tipSection.visibility = View.GONE   // Hide the tip section
                processInputText(inputText)
                binding.mainVIewInputText.text.clear()
            } else {
                Toast.makeText(requireContext(), "Input is empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize Chat RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Set up close button for the tip section
        binding.closeTipButton.setOnClickListener {
            binding.tipSection.visibility = View.GONE   // Hide the tip section
        }

        return view
    }

    private fun processInputText(text: String) {
        // Add user's message to the chat
        chatMessages.add(ChatMessage(text, true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Process the input and display the result
        CoroutineScope(Dispatchers.Main).launch {
            val features = modelHandler.prepareInputs(text)
            val result = modelHandler.runInference(features)

            // Similarity search for given text
            val similarReports = SimilaritySearchUtils.getMessageWithTopSimilarHealthReportIds(text)
            // Add the result as a reply
            val responseText = result.joinToString(separator = "\n") { "${it.first} -> ${it.second}" }
            chatMessages.add(ChatMessage(responseText, false))
            chatMessages.add(ChatMessage(similarReports, false))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

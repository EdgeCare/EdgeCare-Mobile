package com.example.edgecare.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.BertModelHandler
import com.example.edgecare.EdgeCareApp
import com.example.edgecare.ObjectBox
import com.example.edgecare.adapters.ChatAdapter
import com.example.edgecare.databinding.ActivityMainContentBinding
import com.example.edgecare.models.Chat
import com.example.edgecare.models.ChatMessage
import com.example.edgecare.models.ChatMessage_
import com.example.edgecare.utils.SimilaritySearchUtils
import io.objectbox.Box
import io.objectbox.kotlin.equal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class MainContentFragment : Fragment() {

    private var _binding: ActivityMainContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var modelHandler: BertModelHandler
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit  var chatBox: Box<Chat>
    private lateinit  var chatMessageBox: Box<ChatMessage>
    private lateinit var chat : Chat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = ActivityMainContentBinding.inflate(inflater, container, false)
        val view = binding.root

        chatBox = ObjectBox.store.boxFor(Chat::class.java)
        chatMessageBox = ObjectBox.store.boxFor(ChatMessage::class.java)

        //Set to View.GONE to hide the top bar
        binding.topAppBar.visibility = View.VISIBLE

        chat = getOrCreateChat("New Chat")
        val chatList : List<ChatMessage> = getMessagesForChat(chat.id)
        for(chatMessage in chatList){
            chatMessages.add(ChatMessage(message = chatMessage.message, isSentByUser = chatMessage.isSentByUser))
        }
        binding.chatTopic.setText(chat.chatName)

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

        binding.newChatButton.setOnClickListener(){
            chat = newChat()
        }

        var typingTimer: Timer? = null
        val DELAY = 1000L // Delay in milliseconds (e.g., 1 second)

        binding.chatTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel any existing timer to reset the delay
                typingTimer?.cancel()
            }

            override fun afterTextChanged(s: Editable?) {
                typingTimer = Timer()
                typingTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        val newText = s.toString()
                        if (newText.isNotBlank()) {
                            if(newText!= chat.chatName){
                                chat.chatName = newText
                                println("New Chat Name : $newText")
                                chatBox.put(chat)
                            }
                        }
                    }
                }, DELAY) // Start the timer with a 1-second delay
            }
        })


        // Initialize Chat RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Set up close button for the tip section
        binding.closeTipButton.setOnClickListener {
            binding.tipSection.visibility = View.GONE   // Hide the tip section
        }

        return view
    }

    private fun processInputText(text: String) {
        // Add user's message to the chat
        chatMessages.add(ChatMessage(message = text, isSentByUser = true))
        saveMessage(chat.id, text,true)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Process the input and display the result
        CoroutineScope(Dispatchers.Main).launch {
            val features = modelHandler.prepareInputs(text)
            val result = modelHandler.runInference(features)

            // Similarity search for given text
            val similarReports = SimilaritySearchUtils.getMessageWithTopSimilarHealthReportChunkIds(text, requireContext())
            // Add the result as a reply
            val responseText = result.joinToString(separator = "\n") { "${it.first} -> ${it.second}" }
            chatMessages.add(ChatMessage(message = responseText, isSentByUser =  false))
            saveMessage(chat.id, responseText,false)

            chatMessages.add(ChatMessage(message = similarReports, isSentByUser =  false))
            saveMessage(chat.id, similarReports,false)

            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        }
    }

    fun getOrCreateChat(chatName: String): Chat {
        val chatList = chatBox.all
        if(chatList.isEmpty()) {
            return Chat(chatName = chatName).also { chatBox.put(it)}
        }
        else{
            return chatList.last()
        }
    }

    private fun saveMessage(chatId: Long, message: String,  isSentByUser: Boolean) {
        val chatMessage = ChatMessage(
            chatId = chatId,
            message = message,
            timestamp = Date(),
            isSentByUser = isSentByUser
        )
        chatMessageBox.put(chatMessage)
    }

    private fun getMessagesForChat(chatId: Long): List<ChatMessage> {
        return chatMessageBox.query(
            ChatMessage_.chatId equal chatId)
            .build()
            .find()
    }

    private fun newChat():Chat{
        val newChat = Chat()
        chatMessages.removeAll(chatMessages)
        chatAdapter.notifyDataSetChanged()
        binding.chatTopic.setText(newChat.chatName)
        chatBox.put(newChat)
        return newChat
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

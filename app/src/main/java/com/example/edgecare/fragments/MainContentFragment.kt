package com.example.edgecare.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.utils.DeIDModelUtils
import com.example.edgecare.ObjectBox
import com.example.edgecare.adapters.ChatAdapter
import com.example.edgecare.api.sendUserMessage
import com.example.edgecare.databinding.ActivityMainContentBinding
import com.example.edgecare.models.Chat
import com.example.edgecare.models.ChatMessage
import com.example.edgecare.models.ChatMessage_
import com.example.edgecare.models.QueryRequest
import com.example.edgecare.models.QueryResponse
import com.example.edgecare.utils.SimilaritySearchUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.objectbox.Box
import io.objectbox.kotlin.equal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class MainContentFragment : Fragment() {

    private var _binding: ActivityMainContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit  var chatBox: Box<Chat>
    private lateinit  var chatMessageBox: Box<ChatMessage>
    private lateinit var chat : Chat
    private var chatId: Long = 9999L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Retrieve the chatId from arguments
        arguments?.let {
            chatId = it.getLong(ARG_CHAT_ID, 0)
        }

        // Initialize View Binding
        _binding = ActivityMainContentBinding.inflate(inflater, container, false)
        val view = binding.root

        chatBox = ObjectBox.store.boxFor(Chat::class.java)
        chatMessageBox = ObjectBox.store.boxFor(ChatMessage::class.java)

        //Set to View.GONE to hide the top bar
        binding.topAppBar.visibility = View.GONE

        chat = getOrCreateChat(chatId)
        val chatList : List<ChatMessage> = getMessagesForChat(chat.id)
        for(chatMessage in chatList){
            binding.tipSection.visibility = View.GONE   // Hide the tip section
            chatMessages.add(ChatMessage(message = chatMessage.message, isSentByUser = chatMessage.isSentByUser))
        }
        binding.chatTopic.setText(chat.chatName)

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
        val DELAY = 1000L // Delay 1000 milliseconds

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

    companion object {
        private const val ARG_CHAT_ID = "ARG_CHAT_ID"

        fun newInstance(chatId: Long): MainContentFragment {
            val fragment = MainContentFragment()
            val bundle = Bundle()
            bundle.putLong(ARG_CHAT_ID, chatId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun processInputText(text: String) {
        // Add user's message to the chat
        chatMessages.add(ChatMessage(message = text, isSentByUser = true))
        saveMessage(chat.id, text,true)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Process the input and display the result
        CoroutineScope(Dispatchers.Main).launch {

            // Mask text
            val features = DeIDModelUtils.prepareInputs(requireContext(), text)
            val result = DeIDModelUtils.runInference(features)
            val maskedText = result.joinToString(separator = "\n") { "${it.first} -> ${it.second}" }
            chatMessages.add(ChatMessage(message = maskedText, isSentByUser =  false))
            saveMessage(chat.id, maskedText,false)

            // Similarity search for given text
            val similarReports = SimilaritySearchUtils.getMessageWithTopSimilarHealthReportChunkIds(text, requireContext())
            chatMessages.add(ChatMessage(message = similarReports, isSentByUser =  false))
            saveMessage(chat.id, similarReports,false)

            val flowId = "bddaf627-67a6-4e85-8a22-0d73ba257201" // Replace with your flow ID or name
            val langflowId = "e8a827f7-ebab-49a2-8e99-4f3e9b81b6cd" // Replace with your LangFlow ID
            val inputValue = maskedText

            runFlow(flowId, langflowId, inputValue)

            // send to server
            // [TODO] - send maskedText with similarReports
//            sendUserMessage(text) { userMessageResponse ->
//                println("Result from server: $userMessageResponse")
//                if (userMessageResponse != null) {
//                    chatMessages.add(ChatMessage(message = userMessageResponse.body, isSentByUser =  false))
//                }
//            }

            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        }
    }

    private fun getOrCreateChat(chatId: Long): Chat {
        // If chatId == 9999L, we know there can't be an existing Chat with that ID
        if (chatId != 9999L) {
            val existingChat = chatBox.get(chatId)
            if (existingChat != null) {
                return existingChat
            }
            Toast.makeText(requireContext(), "Chat $chatId cannot be found", Toast.LENGTH_SHORT).show()
        }
        // return last Chat
        val chatList = chatBox.all
        if (!chatList.isNullOrEmpty()){
            return chatList.last()
        }
        val newChat = Chat()
        chatMessages.removeAll(chatMessages)
        chatBox.put(newChat)
        return newChat

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
        binding.tipSection.visibility = View.VISIBLE   // Show the tip section
        chatMessages.removeAll(chatMessages)
        chatAdapter.notifyDataSetChanged()
        binding.chatTopic.setText(newChat.chatName)
        chatBox.put(newChat)
        return newChat
    }

    fun runFlow(
        flowId: String,
        langflowId: String,
        inputValue: String,
        inputType: String = "chat",
        outputType: String = "chat",
        tweaks: Map<String, Any> = emptyMap(),
        stream: Boolean = false
    ) {
        val queryRequest = QueryRequest(
            input_value = inputValue,
            input_type = inputType,
            output_type = outputType,
            tweaks = tweaks
        )

        val call = RetrofitClient.api.initiateSession(langflowId, flowId, stream, queryRequest)
        call.enqueue(object : Callback<QueryResponse> {
            override fun onResponse(call: Call<QueryResponse>, response: Response<QueryResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Convert the response body to JSON
                        val jsonString = Gson().toJson(responseBody)

                        // Parse the JSON to extract the message
                        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
                        val outputs = jsonObject.getAsJsonArray("outputs")
                        val finalMessage = outputs
                            ?.firstOrNull()?.asJsonObject
                            ?.getAsJsonArray("outputs")
                            ?.firstOrNull()?.asJsonObject
                            ?.getAsJsonObject("outputs")
                            ?.getAsJsonObject("message")
                            ?.get("text")?.asString

                        if (finalMessage != null) {
                            println("Final Output: $finalMessage")
                            chatMessages.add(ChatMessage(message = finalMessage, isSentByUser =  false))
                            saveMessage(chat.id, finalMessage,false)
                        } else {
                            println("No final message found.")
                        }
                    } else {
                        println("Response body is null.")
                    }
                } else {
                    println("Error: ${response.code()} - ${response.message()}")
                    println("Error Body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<QueryResponse>, t: Throwable) {
                println("Network Error: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

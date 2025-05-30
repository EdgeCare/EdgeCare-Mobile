package com.example.edgecare.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.utils.DeIDModelUtils
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.adapters.ChatAdapter
import com.example.edgecare.api.getChatName
import com.example.edgecare.api.getSampleQuestions
import com.example.edgecare.api.sendUserMessage
import com.example.edgecare.databinding.ActivityMainContentBinding
import com.example.edgecare.models.Chat
import com.example.edgecare.models.ChatMessage
import com.example.edgecare.models.ChatMessage_
import com.example.edgecare.utils.AnonymizationUtils.anonymizeAge
import com.example.edgecare.utils.SimilaritySearchUtils
import io.objectbox.Box
import io.objectbox.kotlin.equal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

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

        chat = getOrCreateChat(chatId)
        val chatList : List<ChatMessage> = getMessagesForChat(chat.id)
        for(chatMessage in chatList){
            binding.tipSection.visibility = View.GONE   // Hide the tip section
            chatMessages.add(ChatMessage(message = chatMessage.message, isSentByUser = chatMessage.isSentByUser))
        }

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

    private fun showSuggestedQuestions(questions: List<String>) {
        binding.suggestedQuestionsContainer.visibility = View.VISIBLE
        val container = view?.findViewById<ConstraintLayout>(R.id.suggestedQuestionsContainer)
        val flow = view?.findViewById<androidx.constraintlayout.helper.widget.Flow>(R.id.suggestedQuestionsFlow)
        val context = container?.context ?: return

        val chipIds = mutableListOf<Int>()
        val inflater = LayoutInflater.from(context)

        // Remove previous views if any
        container?.removeAllViews()
        container?.addView(flow)

        val ThreeQuestions = if (questions.size > 3) questions.take(3) else questions

        ThreeQuestions.forEach { question ->
            val chip = inflater.inflate(R.layout.suggested_question_chip, container, false) as Button
            chip.text = question
            chip.id = View.generateViewId()
            chip.setOnClickListener {
                val inputText = view?.findViewById<EditText>(R.id.mainVIewInputText)
                inputText?.setText(question)
                inputText?.setSelection(question.length)
            }
            chipIds.add(chip.id)
            container?.addView(chip)
        }

        // Update Flow with new chip IDs
        flow?.referencedIds = chipIds.toIntArray()
    }


    private fun processInputText(text: String) {
        setSendButtonLoading(true)

        binding.suggestedQuestionsContainer.visibility = View.GONE

        // Add user's message to the chat
        chatMessages.add(ChatMessage(message = text, isSentByUser = true))
        saveMessage(chat.id, text,true)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        // Process the input and display the result
        CoroutineScope(Dispatchers.Main).launch {

            // Similarity search for given text
            val similarReportsList = SimilaritySearchUtils.getTopSimilarHealthReports(text, requireContext())

            // Mask text
            val features = DeIDModelUtils.prepareInputs(requireContext(), text)
            val result = DeIDModelUtils.runInference(features)

            val maskedString = createMaskedString(result)

//            chatMessages.add(ChatMessage(message = "Masked user message \n "+maskedString, isSentByUser =  false))
//            saveMessage(chat.id, maskedString,false)

            val maskedHealthReportsBuilder = StringBuilder()
            similarReportsList.forEach{report->
                val features2 = DeIDModelUtils.prepareInputs(requireContext(), report)
                val result2 = DeIDModelUtils.runInference(features2)
                val maskedHealthReport = createMaskedString(result2)
                maskedHealthReportsBuilder.append("Report chunk : $maskedHealthReport ,\n \n  ")
            }
            val maskedHealthReports = maskedHealthReportsBuilder.toString()

//            chatMessages.add(ChatMessage(message = "Similar health reports \n "+maskedHealthReports, isSentByUser =  false))
//            saveMessage(chat.id, maskedHealthReports,false)

            if(!isInternetAvailable(requireContext())){
                Toast.makeText(requireContext(), "No internet connection. Please check your network settings.", Toast.LENGTH_SHORT).show()
            }
            else {
                // send to server
                sendUserMessage(chat.id,maskedString,maskedHealthReports,requireContext()) { response ->
                if (response != null) {
                        chatMessages.add(ChatMessage(message = response.content, isSentByUser = false))
                        saveMessage(chat.id, response.content, false)
                        chatAdapter.notifyItemInserted(chatMessages.size - 1)
                        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                        setSendButtonLoading(false)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Unable to connect to the server. Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (chatMessages.size <= 4 || chat.chatName == "New Chat") {
                        setChatName(chat)
                    }
                }
            }
        }
    }

    private fun createMaskedString(input: List<Pair<String, String>>): String {
        val result = StringBuilder()
        var currentLabel: String? = null

        for ((token, label) in input) {
            if ( (label == "O" || label == "TIMESTAMPS" ) && token !="[CLS]" && token != "[SEP]" ) {
                // If label is "O", handle token concatenation based on "##"
                if (token.startsWith("##")) {
//                    result.setLength(result.length - 1) // Remove trailing space
                    result.append(token.removePrefix("##"))
                } else {
                    result.append(" ").append(token)
                }
                currentLabel = null // Reset current label
            } else if(token !="[CLS]" && token != "[SEP]")  {
                // If the label is not "O", add the label and process token
                if (currentLabel != label ) {
                    var newLabel  = ""
                    if (label == "AGE"){
                        val ageRange = token.toIntOrNull()?.let { anonymizeAge(it) }
                        if(ageRange !=null){newLabel = " RANGE : $ageRange YEARS"}
                    }
                    else if (label == "BIRTHDATE") {
                        println("tokentokentoken")
                        println(token)
                        if (token.length == 4 && token.toIntOrNull() != null) {
                            val age = 2025 - token.toInt()
                            val ageRange = anonymizeAge(age)
                            newLabel = " AGE RANGE : $ageRange YEARS"

                        }
                    }
                    result.append(" [").append(label).append(newLabel).append("]")
                    currentLabel = label
                }

            }
        }

        return result.toString().trim()
    }


    private fun getOrCreateChat(chatId: Long): Chat {
        // If chatId == 9999L, we know there can't be an existing Chat with that ID
        if (chatId != 9999L) {
            val existingChat = chatBox.get(chatId)
            if (existingChat != null) {
                return existingChat
            }
//            Toast.makeText(requireContext(), "Chat $chatId cannot be found", Toast.LENGTH_SHORT).show()
        }

        deleteEmptyChats()
        getQuestions()
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
        val messageList= chatMessageBox.query(
            ChatMessage_.chatId equal chatId)
            .build()
            .find()

        println("MessageList"+ messageList.size)
        if(messageList.size>5 && chat.chatName=="New Chat"){
                setChatName(chat)
        }
        return messageList
    }

    private fun setChatName(noNamedchat:Chat){
        context?.let { getChatName(noNamedchat.id, it) { response ->
            noNamedchat.chatName = response?.chatName.toString()
            if(noNamedchat.chatName != "null") {
                chatBox.put(noNamedchat)
                println(noNamedchat.chatName)
            }
        }
        }
    }

    private fun getQuestions(){
        context?.let { getSampleQuestions(it) { response ->
            if (response != null && response.questions.isNotEmpty() && chatMessages.isEmpty()) {
                println("question list size"+response.questions.size)
                println(response.questions)
                showSuggestedQuestions(response.questions)
            }
        }
        }
    }

    private fun deleteEmptyChats() {
        val chats = chatBox.all

        for (chat in chats) {
            val messageCount = chatMessageBox.query()
                .equal(ChatMessage_.chatId, chat.id)
                .build()
                .count()

            if (messageCount == 0L) {
                chatBox.remove(chat)
            }
        }
    }

    private fun setSendButtonLoading(isLoading: Boolean) {
        binding.sendButton.isEnabled = !isLoading
        if (isLoading){
            binding.sendLoading.visibility = View.VISIBLE
        } else {
            binding.sendLoading.visibility = View.GONE
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

package com.example.edgecare.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.adapters.ChatAdapter
import com.example.edgecare.databinding.FragmentOfflineChatBinding
import com.example.edgecare.models.Chat
import com.example.edgecare.models.ChatMessage
import com.example.edgecare.models.ChatMessage_
import com.example.edgecare.models.SmallModelinfo
import com.example.edgecare.utils.SimilarReportChunk
import com.example.edgecare.utils.SimilaritySearchUtils
import com.example.smollm.GGUFReader
import io.objectbox.Box
import io.objectbox.kotlin.equal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val LOGTAG = "[ChatActivity]"
private val LOGD: (String) -> Unit = { Log.d(LOGTAG, it) }

class OfflineChatFragment : Fragment() {

    private var _binding: FragmentOfflineChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit  var chatBox: Box<Chat>
    private lateinit  var chatMessageBox: Box<ChatMessage>
    private lateinit var chat : Chat
    private var chatId: Long = 9999L

    private var modelUnloaded = false
    private lateinit var smolLMManager: com.example.edgecare.utils.SmolLMManager
    private val findThinkTagRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)

    private val FILE_PICK_REQUEST_CODE = 1001
    private lateinit var progressContainer: LinearLayout
    private lateinit var modelInfoBox: Box<SmallModelinfo>
    private val similarityThreshold = 0.15f

    val dateFormat = SimpleDateFormat("M/d-HH:mm", Locale.getDefault())
    val currentTime = dateFormat.format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Init Smollm Manage
        smolLMManager = com.example.edgecare.utils.SmolLMManager()
        //Init Box for model Info
        modelInfoBox=ObjectBox.store.boxFor(SmallModelinfo::class.java)
        // Check model is available
        val isNotModelAvailable = modelInfoBox.isEmpty
        if (isNotModelAvailable) {
            Toast.makeText(requireContext(), "Please Select GGUF model", Toast.LENGTH_LONG).show()
            startFilePicker()
        }else{
            Toast.makeText(requireContext(), "Model Detected!", Toast.LENGTH_SHORT).show()
            //Suspended function for model loading
            lifecycleScope.launch {
                loadModel()
            }
        }

        // Retrieve the chatId from arguments
        arguments?.let {
            chatId = it.getLong(ARG_CHAT_ID, 0)
        }

        // Initialize View Binding
        _binding = FragmentOfflineChatBinding.inflate(inflater,container,false)
        val view = binding.root

        progressContainer = view.findViewById(R.id.progressContainer)

        chatBox = ObjectBox.store.boxFor(Chat::class.java)
        chatMessageBox = ObjectBox.store.boxFor(ChatMessage::class.java)

        chat = getOrCreateChat(chatId)
        val chatList : List<ChatMessage> = getMessagesForChat(chat.id)
        for(chatMessage in chatList){
            binding.tipSection.visibility = View.GONE   // Hide the tip section
            chatMessages.add(ChatMessage(message = chatMessage.message, isSentByUser = chatMessage.isSentByUser, isLocalChat = chatMessage.isLocalChat))
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

    private fun startFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE)
    }

    // Get file result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            // handle selected file
            if (selectedFileUri != null) {
                copyModelFile(selectedFileUri)
                //Toast.makeText(requireContext(), "File picked: $selectedFileUri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyModelFile(
        uri: Uri
    ) {
        var fileName = ""
        context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        if (fileName.isNotEmpty()) {
            progressContainer.visibility = View.VISIBLE //show progress
            CoroutineScope(Dispatchers.IO).launch {
                context?.contentResolver?.openInputStream(uri).use { inputStream ->
                    FileOutputStream(File(context?.filesDir, fileName)).use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }
                val ggufReader = GGUFReader()
                ggufReader.load(File(context?.filesDir, fileName).absolutePath)
                val contextSize = ggufReader.getContextSize() ?: -1
                val chatTemplate = ggufReader.getChatTemplate() ?: ""

                // Create a new model info
                val newModel = SmallModelinfo(
                    name = fileName,
                    url = "",
                    path = Paths.get(context?.filesDir?.absolutePath, fileName).toString(),
                    contextSize = contextSize.toInt(),
                    chatTemplate = chatTemplate
                )

                // Add it to the database
                modelInfoBox.put(newModel)
                withContext(Dispatchers.Main) {
                    progressContainer.visibility = View.GONE  // hide progress
                    Toast.makeText(requireContext(), "Model copied successfully!", Toast.LENGTH_SHORT).show()
                    //Suspended function for model loading
                    lifecycleScope.launch {
                        loadModel()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Invalid_file)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processInputText(text: String) {

        // Similarity search for given text
        val similarReportsDataList = SimilaritySearchUtils.getSimilarHealthReportsWithAdditionalInfo(text, requireContext())
        //filter out by a threshold
        val similarReportsList = similarReportsDataList
            .filter { it.similarity > similarityThreshold }
            .map { it.text }

        val prompt = buildPromptForChatbotWithUserMessage(text,similarReportsList)
        val referred_reports_str=formatSimilarReports(similarReportsList)

        // Add user's message to the chat
        chatMessages.add(ChatMessage(message = text, isSentByUser = true, isLocalChat = true))
        saveMessage(chat.id, text, isSentByUser = true, isLocalChat = false)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        //send query and get response

        // Add a "Thinking..." message first
        chatMessages.add(ChatMessage(message = "Thinking...", isSentByUser = false, isLocalChat = false, additionalInfo = referred_reports_str))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        // Save the index where "Thinking..." is added
        val thinkingMessageIndex = chatMessages.lastIndex

            smolLMManager.getResponse(
                prompt,
                responseTransform = {findThinkTagRegex.replace(it) { matchResult ->
                    "<blockquote>${matchResult.groupValues[1]}</blockquote>"
                }
                },
                onPartialResponseGenerated = { partialResponseText ->
                    // directly update your chat bubble with the latest partial text
                    // Update the message at thinkingMessageIndex
                    chatMessages[thinkingMessageIndex] = ChatMessage(message = partialResponseText, isSentByUser = false, isLocalChat = true, additionalInfo = referred_reports_str)
                    chatAdapter.notifyItemChanged(thinkingMessageIndex)
                    binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                },
                onSuccess = { response ->

                    if (thinkingMessageIndex in chatMessages.indices) {
                        chatMessages[thinkingMessageIndex] = ChatMessage(message = response.response, isSentByUser = false, isLocalChat = true, additionalInfo = referred_reports_str)
                        saveMessage(chat.id, response.response,
                            isSentByUser = false,
                            isLocalChat = true
                        )
                        chatAdapter.notifyItemChanged(thinkingMessageIndex)
                        binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                    } else {
                        // No action. Don't create extra bubble.
                        Log.w("Chat", "Thinking message missing. Ignoring final update.")
                    }
                },
                onCancelled = {
                    // ignore CancellationException, as it was called because
                    // `responseGenerationJob` was cancelled in the `stopGeneration` method
                },
                onError = { exception ->
                    Toast.makeText(requireContext(), "Something Went wrong", Toast.LENGTH_SHORT)
                        .show()
                    LOGD("Generating Error: $exception")
                },
            )

    }

    fun formatSimilarReports(similarReportsList: List<String>): String {
        if (similarReportsList.isEmpty()) {
            return "No reports used"
        }

        return similarReportsList.joinToString(separator = "\n===========Next report chunk:\n") { item ->
            if (item.length > 200) item.substring(0, 200) else item
        }
    }


    fun buildPromptForChatbotWithUserMessage(
        userMessage: String,
        similarReportsList: List<String>
    ): String {
        val promptBuilder = StringBuilder()

        if (similarReportsList.isEmpty()) {
            val initPrompt="You're a helpful friend. Just talk like a normal person in everyday conversation. No code, no technical stuff unless asked. Respond casually like you're chatting with someone you know."
            promptBuilder.append(initPrompt)
            promptBuilder.append("User says: ")
            promptBuilder.append("\"$userMessage\"\n\n")
        } else {
                promptBuilder.append("A user has submitted the following message describing their health condition:\n")
                promptBuilder.append("\"$userMessage\"\n\n")
                promptBuilder.append("Here are health report summaries similar to the user's message. ")
                //promptBuilder.append("Based on the user's input and the following reports, provide relevant medical insights, possible diagnoses, or suggestions:\n\n")

                similarReportsList.forEachIndexed { index, report ->
                    promptBuilder.append("Report ${index + 1}:\n$report\n\n")
                }

                promptBuilder.append("Taking into account the user's message and these reports, what would be your professional medical advice?")

        }

        return promptBuilder.toString()
    }



    fun loadModel() {
        // clear resources occupied by the previous model
        smolLMManager.close()

        val modelInfo: SmallModelinfo? = modelInfoBox.all.firstOrNull()

        if (modelInfo == null) {
            //Todo: Navigate to Insert Model Activity
            Toast.makeText(requireContext(), "Please insert the GGUF model", Toast.LENGTH_SHORT).show()
            return //temp
        }

        smolLMManager.create(
            com.example.edgecare.utils.SmolLMManager.SmolLMInitParams(
                modelInfo.path,
                0.05f,
                0.2f, //reduced temp for more factual responses
                false,
                modelInfo.contextSize.toLong(),//context length auto set by the model
                modelInfo.chatTemplate,
                4,
                true,
                false,
            ),
            onError = { e ->
                Toast.makeText(requireContext(), "Model loading failed!", Toast.LENGTH_SHORT).show()

            },
            onSuccess = {
                Toast.makeText(requireContext(), "Model loaded successfully!", Toast.LENGTH_SHORT).show()
                //initConversation()

            },
        )
    }

    fun initConversation(){
        val initPrompt="You're a helpful friend. Just talk like a normal person in everyday conversation. No code, no technical stuff unless asked. Respond casually like you're chatting with someone you know."
        smolLMManager.getResponse(
            initPrompt,
            responseTransform = {findThinkTagRegex.replace(it) { matchResult ->
                "<blockquote>${matchResult.groupValues[1]}</blockquote>"
            }
            },
            onPartialResponseGenerated = { partialResponseText ->
            },
            onSuccess = { response ->
                Toast.makeText(requireContext(), "Conversation initiated successfully!", Toast.LENGTH_SHORT).show()
                println("Init response = $response")
            },
            onCancelled = {
                // ignore CancellationException, as it was called because
                // `responseGenerationJob` was cancelled in the `stopGeneration` method
            },
            onError = { exception ->
                Toast.makeText(requireContext(), "Initializing the conversation is failed", Toast.LENGTH_SHORT)
                    .show()
                LOGD("Generating Error: $exception")
            },
        )
    }

    /**
     * Clears the resources occupied by the model only
     * if the inference is not in progress.
     */
    private fun unloadModel(): Boolean =
        if (!smolLMManager.isInferenceOn) {
            smolLMManager.close()
            //_modelLoadState.value = ModelLoadingState.NOT_LOADED
            true
        } else {
            false
        }

    private fun saveMessage(chatId: Long, message: String,  isSentByUser: Boolean, isLocalChat:Boolean) {
        val chatMessage = ChatMessage(
            chatId = chatId,
            message = message,
            timestamp = Date(),
            isSentByUser = isSentByUser,
            isLocalChat = isLocalChat
        )
        chatMessageBox.put(chatMessage)
    }

    companion object {
        private const val ARG_CHAT_ID = "ARG_CHAT_ID"

        fun newInstance(chatId: Long): OfflineChatFragment {
            val fragment = OfflineChatFragment()
            val bundle = Bundle()
            bundle.putLong(ARG_CHAT_ID, chatId)
            fragment.arguments = bundle
            return fragment
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

        deleteEmptyChats()
        val newChat = Chat()

        newChat.isOffline=true
        newChat.chatName="Offline Chat@ $currentTime"

        chatMessages.removeAll(chatMessages)
        chatBox.put(newChat)
        return newChat
    }

    private fun getMessagesForChat(chatId: Long): List<ChatMessage> {
        return chatMessageBox.query(
            ChatMessage_.chatId equal chatId)
            .build()
            .find()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
    override fun onStart() {
        super.onStart()
        if (modelUnloaded) {
            loadModel()
            LOGD("onStart() called - model loaded")
        }
    }

    override fun onStop() {
        super.onStop()
        modelUnloaded = unloadModel()
        LOGD("onStop() called - model unloaded result: $modelUnloaded")
    }

}
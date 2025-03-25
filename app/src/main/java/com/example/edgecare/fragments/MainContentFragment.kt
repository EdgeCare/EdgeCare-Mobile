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
import com.example.edgecare.utils.AnonymizationUtils.anonymizeAge
import com.example.edgecare.utils.AnonymizationUtils.calculateAgeFromYear
import com.example.edgecare.utils.CPUMonitor
import com.example.edgecare.utils.LatencyLogger
import com.example.edgecare.utils.RAMMonitor
import com.example.edgecare.utils.SimilaritySearchUtils
import io.objectbox.Box
import io.objectbox.kotlin.equal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    override fun onCreateView  (
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
            CoroutineScope(Dispatchers.Main).launch {
                val inputText = binding.mainVIewInputText.text.toString()
                if (inputText.isNotEmpty()) {
                    binding.tipSection.visibility = View.GONE   // Hide the tip section
                    val questions = arrayOf(
                        "I've been feeling unusually tired every day, even after a full night's sleep, and I don't do anything too physically demanding—just light housework and a desk job—could this be a sign of something serious or just stress-related?",
                        "Lately, I’ve been getting frequent headaches in the late afternoon, especially after spending most of the day on my laptop for work—could this be due to screen time or something else I should get checked?",
                        "I’ve noticed that after I eat, especially dinner, I tend to get bloated and feel uncomfortable for hours—my meals are usually pretty simple, so I’m wondering if this could be a digestion issue or maybe a food intolerance?",
                        "Every morning when I wake up and stand up quickly, I feel a bit dizzy and sometimes need to sit down again—my diet and water intake seem fine to me, but should I be concerned about low blood pressure or something else?",
                        "I work out 3–4 times a week, mostly cardio and light weights, but recently I’ve been having mild chest discomfort afterward—it's not severe, but it’s enough to make me worried, should I see a cardiologist?",
                        "Over the past few weeks, I've been feeling anxious without any clear reason, and it’s starting to affect my concentration at work and my sleep—could this be a sign of an anxiety disorder, or should I look at my lifestyle first?",
                        "My sleep has been really disturbed lately—I go to bed around the same time every night, but I wake up several times for no reason and feel exhausted the next day—could this be a sleep disorder?",
                        "I’m generally a healthy eater, but I’ve noticed that every time I eat dairy products, I get mild stomach cramps and sometimes diarrhea—should I be tested for lactose intolerance or could this be something else?",
                        "For the past month, I’ve been experiencing joint stiffness in the mornings that usually improves by midday—I’m 36 and moderately active, so I’m wondering if this could be early arthritis or just a temporary issue?",
                        "I've had a persistent dry cough for about two weeks now—it’s not severe, but it hasn’t gone away, and I don’t smoke or have allergies—should I be concerned about something like asthma or even COVID?",
                        "I spend most of my day sitting for work, and I’ve started to notice pain in my lower back by evening—I've tried adjusting my chair and posture, but it doesn’t seem to help much—what should I do to prevent this from getting worse?",
                        "After I exercise, especially when it’s intense or includes running, I sometimes feel a tightness in my chest and wheezing—does this mean I could have exercise-induced asthma or should I get some tests done?",
                        "My heart sometimes feels like it's skipping a beat or fluttering, especially when I'm resting in bed at night—it only lasts a few seconds but happens a few times a week—should I be worried about arrhythmia?",
                        "Even though I eat regularly and include vegetables and protein in my meals, I’ve been losing weight unintentionally over the past two months—could this be a metabolic issue or something more serious?",
                        "I’ve been having frequent urges to urinate even when I haven’t had much water to drink, and sometimes there's a slight burning sensation—could this be a UTI or related to something else like blood sugar levels?",
                        "Most evenings, I feel a dull ache in my legs, especially after sitting or standing for too long—I'm not very active during the day, but I do walk occasionally—could this be poor circulation or something related to veins?",
                        "My skin has become itchy and dry all over, and no matter how much moisturizer I use, it doesn’t seem to help—I haven’t changed soaps or detergents—could this be a sign of a skin condition or something internal like thyroid issues?",
                        "I’ve been feeling more irritable and low-energy during the day, even though I haven’t made any major changes to my routine—is it possible this is related to hormones, or should I consider mental health support?",
                        "Recently, I’ve noticed my hands tremble slightly when I try to hold something still, like a cup or my phone—it’s not all the time, but enough to notice—could this be a neurological issue or just stress?",
                        "I feel like I’m constantly hungry even shortly after eating, and I’ve also been more thirsty than usual—I'm not eating excessively sugary foods, so I’m wondering if I should get checked for diabetes?"
                    )

                    for (question in questions) {
                        processInputText(question)
                        delay(60000L)
                    }


                    //                processInputText(inputText)
                    binding.mainVIewInputText.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Input is empty", Toast.LENGTH_SHORT).show()
                }
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
            LatencyLogger.start("Total End-to-End Latency")
            // Similarity search for given text
            LatencyLogger.start("Health Reports Retrieving")
            val similarReportsList = SimilaritySearchUtils.getMessageWithTopSimilarHealthReportChunkIds(text, requireContext())
            LatencyLogger.end("Health Reports Retrieving")
            CPUMonitor.logCPUUsage("After Health Reports Retrieval")
            RAMMonitor.logRAMUsage(requireContext(), "After Health Reports Retrieval")

            // Mask text
            LatencyLogger.start("Masking user Input and health reports")
            val features = DeIDModelUtils.prepareInputs(requireContext(), text)
            val result = DeIDModelUtils.runInference(features)
            val maskedString = createMaskedString(result)
            chatMessages.add(ChatMessage(message = maskedString, isSentByUser =  false))
            saveMessage(chat.id, maskedString,false)

            val maskedHealthReportsBuilder = StringBuilder()
            similarReportsList.forEach{report->
//                CPUMonitor.logCPUUsage("Masking")
                val features2 = DeIDModelUtils.prepareInputs(requireContext(), report)
                val result2 = DeIDModelUtils.runInference(features2)
                val maskedHealthReport = createMaskedString(result2)
                maskedHealthReportsBuilder.append("Report chunk : $maskedHealthReport ,\n \n  ")
            }
            val maskedHealthReports = maskedHealthReportsBuilder.toString()
            chatMessages.add(ChatMessage(message = maskedHealthReports, isSentByUser =  false))
            saveMessage(chat.id, maskedHealthReports,false)

            LatencyLogger.end("Masking user Input and health reports")
            CPUMonitor.logCPUUsage("After Masking")
            RAMMonitor.logRAMUsage(requireContext(), "After Masking")


            // send to server
            LatencyLogger.start("API Request Delay")
            sendUserMessage(chat.id,maskedString,maskedHealthReports,requireContext()) { response ->
                if (response != null) {
                    LatencyLogger.end("API Request Delay")
                    chatMessages.add(ChatMessage(message = response.content, isSentByUser =  false))
                    saveMessage(chat.id, response.content,false)
                }

                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                LatencyLogger.end("Total End-to-End Latency")
                CPUMonitor.logCPUUsage("After Answer display")
                RAMMonitor.logRAMUsage(requireContext(), "After Answer display")
            }

        }
    }

    private fun createMaskedString(input: List<Pair<String, String>>): String {
        val result = StringBuilder()
        var currentLabel: String? = null

        for ((token, label) in input) {
            if (label == "O" && token !="[CLS]" && token != "[SEP]") {
                // If label is "O", handle token concatenation based on "##"
                if (token.startsWith("##")) {
                    result.setLength(result.length - 1) // Remove trailing space
                    result.append(token.removePrefix("##"))
                } else {
                    result.append(token).append(" ")
                }
                currentLabel = null // Reset current label
            } else if(token !="[CLS]" && token != "[SEP]")  {
                // If the label is not "O", add the label and process token
                if (currentLabel != label ) {
                    var newLabel  = ""
                    if(label == "AGE"){
                        val ageRange = token.toIntOrNull()?.let { anonymizeAge(it) }
                        if(ageRange !=null){newLabel = " RANGE : $ageRange YEARS"}
                    }
//                    else if(label == "BIRTHDAY"){
//                        val age = calculateAgeFromYear(token)
//                        val ageRange = age?.let { anonymizeAge(it) }
//                        if(ageRange !=null){newLabel = " AGE RANGE : $ageRange YEARS"}
//                    }
                    result.append("[").append(label).append(newLabel).append("] ")
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
        LatencyLogger.logSummary()
        CPUMonitor.logAverageUsage()
        CPUMonitor.resetUsageData()
        RAMMonitor.logAverageRAMUsage()
        RAMMonitor.resetUsageData()
        return newChat
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

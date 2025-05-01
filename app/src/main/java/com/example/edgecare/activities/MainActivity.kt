package com.example.edgecare.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.adapters.ChatHistoryAdapter
import com.example.edgecare.databinding.ActivitySideBarBinding
import com.example.edgecare.fragments.MainContentFragment
import com.example.edgecare.fragments.OfflineChatFragment
import com.example.edgecare.fragments.PersonaFragment
import com.example.edgecare.fragments.ReportHandleFragment
import com.example.edgecare.models.Chat
import io.objectbox.Box

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySideBarBinding
    private lateinit var chatBox: Box<Chat>
    private var selectedChatId:Long = 999L          // set for 999 temporary
    private lateinit var adapter:ChatHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivitySideBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup main buttons in side nav bar
        setupStaticSidebarButtons()

        // Load MainContentFragment into the FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatContentFrame, MainContentFragment())
            .commit()

        // Default side navbar button selection
        selectButton(binding.btnNewEdgeCare.id)

        // Query your Chat data from ObjectBox and Set up the RecyclerView in the sidebar
        chatBox = ObjectBox.store.boxFor(Chat::class.java)
        val allChats = chatBox.all
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatHistoryAdapter(
            initialChats = allChats.asReversed(),
            onChatClicked = { chat ->
                // On click, pass Chat.id to MainContentFragment
                selectButton(-1)
                selectedChatId = chat.id
                openChatById(chat.id)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        )
        binding.historyRecyclerView.adapter = adapter

        // Set up the sidebar toggle button
        binding.sidebarToggleButton.setOnClickListener {
            val allChats2 = chatBox.all.asReversed()
            adapter.updateChatList(allChats2,selectedChatId)
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun openChatById(chatId: Long) {
        selectedChatId = chatId
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatContentFrame, MainContentFragment.newInstance(chatId))
            .commit()
    }

    private fun selectButton(selectedMenuId: Int) {
        val buttons = listOf(
            binding.btnNewEdgeCare,
            binding.personaButton,
            binding.healthReportsButton,
            binding.btnAppSettings,
            binding.btnOfflineChat
        )

        buttons.forEach { button ->
            if (button.id == selectedMenuId) {
                button.setBackgroundResource(R.drawable.side_nav_bar_selected_button_background)
            } else {
                button.setBackgroundResource(R.drawable.side_nav_bar_button_background)
            }
        }
    }

    private fun setupStaticSidebarButtons() {
        binding.btnNewEdgeCare.setOnClickListener {
            adapter.chatSelected(true)
            selectedChatId = chatBox.all.last().id+1

            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, MainContentFragment())
                .addToBackStack(null) // Add to back stack for navigation (optional)
                .commit()
            selectButton(-1)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.personaButton.setOnClickListener {
            selectedChatId = 999L
            adapter.chatSelected(false)
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, PersonaFragment())
//                .replace(R.id.chatContentFrame, PersonaNewFragment())
                .addToBackStack(null) // Add to back stack for navigation (optional)
                .commit()
            selectButton(R.id.personaButton)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.healthReportsButton.setOnClickListener {
            selectedChatId = 999L
            adapter.chatSelected(false)
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, ReportHandleFragment())
                .addToBackStack(null) // Add to back stack for navigation (optional)
                .commit()
            selectButton(binding.healthReportsButton.id)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.btnOfflineChat.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, OfflineChatFragment())
                .addToBackStack(null) // Add to back stack for navigation (optional)
                .commit()
            selectButton(binding.btnOfflineChat.id)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.btnAppSettings.setOnClickListener {
            Toast.makeText(this, "Will be available soon", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, ___Activity::class.java))
            selectButton(R.id.btn_app_settings)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}

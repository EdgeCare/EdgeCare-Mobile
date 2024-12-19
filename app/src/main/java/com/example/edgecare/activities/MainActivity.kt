package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.edgecare.R
import com.example.edgecare.databinding.ActivityTopBarBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityTopBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the sidebar toggle button
        binding.sidebarToggleButton.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up button clicks in the sidebar
        binding.btnNewEdgeCare.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, MainContentFragment())
                .addToBackStack(null) // Add to back stack for navigation (Mr.t -> optional - not necessarily need)
                .commit()
            selectButton(binding.btnNewEdgeCare.id)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.btnPersonaActivity.setOnClickListener {
            startActivity(Intent(this, CollectPersonaDataActivity::class.java))
            selectButton(R.id.btnPersonaActivity)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.selectFileButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, ReportHandleFragment())
                .addToBackStack(null) // Add to back stack for navigation (Mr.t -> optional - not necessarily need)
                .commit()
            selectButton(binding.selectFileButton.id)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Load MainContentFragment into the FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatContentFrame, MainContentFragment())
            .commit()

        // Default side navbar button selection
        selectButton(binding.btnNewEdgeCare.id)
    }

    private fun selectButton(selectedId: Int) {
        val buttons = listOf(
            binding.btnNewEdgeCare,
            binding.btnPersonaActivity,
            binding.selectFileButton
        )

        buttons.forEach { button ->
            if (button.id == selectedId) {
                button.setBackgroundResource(R.drawable.side_nav_bar_selected_button_background)
            } else {
                button.setBackgroundResource(R.drawable.side_nav_bar_button_background)
            }
        }
    }
}

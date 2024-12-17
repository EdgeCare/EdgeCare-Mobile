package com.example.edgecare.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.databinding.ActivityTopBarBinding
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopBarBinding
    private lateinit var healthReportBox: Box<HealthReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityTopBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ObjectBox
        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

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
            checkPermissionsAndSelectFile()
            selectButton(R.id.selectFileButton)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.viewReportsButton.setOnClickListener {
            startActivity(Intent(this, ReportListActivity::class.java))
            selectButton(R.id.viewReportsButton)
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
            binding.selectFileButton,
            binding.viewReportsButton
        )

        buttons.forEach { button ->
            if (button.id == selectedId) {
                button.setBackgroundResource(R.drawable.side_nav_bar_selected_button_background)
            } else {
                button.setBackgroundResource(R.drawable.side_nav_bar_button_background)
            }
        }
    }

    private fun checkPermissionsAndSelectFile() {
        // TODO: Check if permission is granted; if not, request permission.
        selectTextFile()
    }

    private fun selectTextFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> saveHealthReport(uri) }
        }
    }

    private fun saveHealthReport(fileUri: Uri) {
        try {
            val text = FileUtils.readTextFile(this.contentResolver, fileUri)
            val embedding = EmbeddingUtils.computeEmbedding(text)
            if (embedding != null) {
                val report = HealthReport(text = text, embedding = embedding)
                healthReportBox.put(report)
                Toast.makeText(this, "Health report saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save health report", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save health report", Toast.LENGTH_SHORT).show()
        }
    }
}

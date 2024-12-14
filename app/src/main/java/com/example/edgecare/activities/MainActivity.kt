package com.example.edgecare.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var healthReportBox: Box<HealthReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_bar)

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)

        // Set up ObjectBox
        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

        // Set up the sidebar toggle button
        val sidebarToggle = findViewById<ImageButton>(R.id.sidebarToggle)
        sidebarToggle.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up button clicks in the sidebar
        findViewById<Button>(R.id.btn_persona_activity).setOnClickListener {
            startActivity(Intent(this, CollectPersonaDataActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.selectFileButton).setOnClickListener {
            checkPermissionsAndSelectFile()
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.viewReportsButton).setOnClickListener {
            startActivity(Intent(this, ReportListActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Load MainContentActivity into the FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatContentFrame, MainContentFragment())
            .commit()
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

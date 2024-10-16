package com.example.edgecare.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box

class MainActivity : AppCompatActivity() {

    private lateinit var selectFileButton: Button
    private lateinit var healthReportBox: Box<HealthReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)
        selectFileButton = findViewById(R.id.selectFileButton)

        // Set click listener on the button
        selectFileButton.setOnClickListener {
            checkPermissionsAndSelectFile()
        }

        val button = findViewById<Button>(R.id.btn_persona_activity)
        button.setOnClickListener {
            val intent = Intent(this, CollectPersonaDataActivity::class.java)
            startActivity(intent)
        }

        val viewReportsButton  = findViewById<Button>(R.id.viewReportsButton)
        viewReportsButton.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissionsAndSelectFile() {
        // Check if permission is granted
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
            // Permission is granted; proceed to select file
            selectTextFile()
//        } else {
//            // Request permission
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                PERMISSION_REQUEST_CODE
//            )
//        }
    }

    private fun selectTextFile() {
        // Create an intent to select a text file
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        filePickerLauncher.launch(intent)
    }

    // Activity Result API for file picker
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result of the file picker
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                // Save the health report
                saveHealthReport(uri)
            }
        }
    }

    private fun saveHealthReport(fileUri: Uri) {
        try {
            // Read text from the selected file
            val text = FileUtils.readTextFile(contentResolver, fileUri)
            // Compute embedding
            val embedding = EmbeddingUtils.computeEmbedding(text)
            // Create HealthReport object
            val report = HealthReport(text = text, embedding = embedding)
            // Save to ObjectBox
            healthReportBox.put(report)
            Toast.makeText(this, "Health report saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save health report", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission granted; proceed to select file
                selectTextFile()
            } else {
                Toast.makeText(this, "Permission denied to read files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}

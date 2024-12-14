package com.example.edgecare.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.edgecare.BertModelHandler
import com.example.edgecare.EdgeCareApp
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainContentFragment : Fragment() {

    private lateinit var selectFileButton: Button
    private lateinit var healthReportBox: Box<HealthReport>
    private lateinit var modelHandler: BertModelHandler
    private lateinit var outputTextView: TextView
    private lateinit var sendButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main_content, container, false)

        // Initialize UI components
        val inputEditText = view.findViewById<EditText>(R.id.mainVIewInputText)
        outputTextView = view.findViewById(R.id.mainVIewOutputText)
        selectFileButton = view.findViewById(R.id.selectFileButton)
        sendButton = view.findViewById(R.id.sendButton)

        // Set up ObjectBox
        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

        // Initialize BERT model handler
        modelHandler = (requireActivity().application as EdgeCareApp).modelHandler

        // Set up click listener for the send button
        sendButton.setOnClickListener {
            val inputText = inputEditText.text.toString()
            if (inputText.isNotEmpty()) {
                processInputText(inputText)
                inputEditText.text.clear() // Clear input after processing
            } else {
                Toast.makeText(requireContext(), "Input is empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up button listeners
        selectFileButton.setOnClickListener { checkPermissionsAndSelectFile() }

        view.findViewById<Button>(R.id.btn_persona_activity).setOnClickListener {
            startActivity(Intent(requireContext(), CollectPersonaDataActivity::class.java))
        }

        view.findViewById<Button>(R.id.viewReportsButton).setOnClickListener {
            startActivity(Intent(requireContext(), ReportListActivity::class.java))
        }

        return view
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
            val text = FileUtils.readTextFile(requireActivity().contentResolver, fileUri)
            val embedding = EmbeddingUtils.computeEmbedding(text)
            if (embedding != null) {
                val report = HealthReport(text = text, embedding = embedding)
                healthReportBox.put(report)
                Toast.makeText(requireContext(), "Health report saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save health report", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save health report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processInputText(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val features = modelHandler.prepareInputs(text)
            val result = modelHandler.runInference(features)
            displayResults(result)
        }
    }

    private fun displayResults(labeledTokens: List<Pair<String, String>>) {
        val resultBuilder = StringBuilder()
        labeledTokens.forEach { (token, label) ->
            resultBuilder.append("$token -> $label\n")
        }
        outputTextView.text = resultBuilder.toString()
    }
}

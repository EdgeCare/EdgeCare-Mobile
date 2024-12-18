package com.example.edgecare.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.ObjectBox
import com.example.edgecare.adapters.HealthReportAdapter
import com.example.edgecare.databinding.ActivityReportHandleBinding
import com.example.edgecare.models.HealthReport
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box

class ReportHandleFragment : Fragment() {
    private var _binding: ActivityReportHandleBinding? = null
    private val binding get() = _binding!!
    private lateinit var healthReportBox: Box<HealthReport>
    private lateinit var adapter: HealthReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityReportHandleBinding.inflate(inflater, container, false)
        val view = binding.root

        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

        binding.selectFileButton.setOnClickListener {
            checkPermissionsAndSelectFile()
        }

        loadHealthReports()

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
            val text = FileUtils.readTextFile(requireContext().contentResolver, fileUri)
            val embedding = EmbeddingUtils.computeEmbedding(text)
            if (embedding != null) {
                val report = HealthReport(text = text, embedding = embedding)
                healthReportBox.put(report)
                loadHealthReports()
                Toast.makeText(requireContext(), "Health report saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save health report", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save health report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HealthReportAdapter { report ->
            // Handle item click
            val intent = Intent(requireContext(), ViewFullReportActivity::class.java)
            intent.putExtra("report_id", report.id)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun loadHealthReports() {
        val reports = healthReportBox.all
        if(reports.size>0){
            setupRecyclerView()
            adapter.submitList(reports)
            binding.noHealthReportsText.text = ""
            binding.linearLayout2.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
        else {
            binding.recyclerView.visibility = View.GONE
            binding.linearLayout2.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}
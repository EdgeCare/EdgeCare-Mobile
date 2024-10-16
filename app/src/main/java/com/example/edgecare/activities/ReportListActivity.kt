package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.ObjectBox
import com.example.edgecare.adapters.HealthReportAdapter
import com.example.edgecare.models.HealthReport
import com.example.edgecare.databinding.ActivityReportListBinding
import io.objectbox.Box

class ReportListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportListBinding
    private lateinit var healthReportBox: Box<HealthReport>
    private lateinit var adapter: HealthReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

        setupRecyclerView()
        loadHealthReports()
    }
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HealthReportAdapter { report ->
            // Handle item click
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("report_id", report.id)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun loadHealthReports() {
        val reports = healthReportBox.all
        adapter.submitList(reports)
    }
}
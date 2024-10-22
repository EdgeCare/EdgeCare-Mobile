package com.example.edgecare.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.ObjectBox
import com.example.edgecare.models.HealthReport
import com.example.edgecare.databinding.ActivityReportDetailBinding
import io.objectbox.Box


class ReportDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportDetailBinding
    private lateinit var healthReportBox: Box<HealthReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)

        val reportId = intent.getLongExtra("report_id", -1)
        if (reportId != -1L) {
            displayReport(reportId)
        } else {
            // Handle error
            binding.textViewContent.text = "Report not found."
        }
    }

    private fun displayReport(reportId: Long) {
        val report = healthReportBox.get(reportId)
        if (report != null) {
            binding.textViewTitle.text = "Report ID: ${report.id}"
            binding.textViewContent.text = report.text
        } else {
            binding.textViewContent.text = "Report not found."
        }
    }
}

package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.edgecare.ObjectBox
import com.example.edgecare.models.HealthReport
import com.example.edgecare.databinding.ActivityViewFullReportBinding
import io.objectbox.Box
import java.io.File


class ViewFullReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewFullReportBinding
    private lateinit var healthReportBox: Box<HealthReport>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFullReportBinding.inflate(layoutInflater)
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
        if(report.isPDF){
            viewPdfFromObjectBox(reportId)
        }
        if (report != null) {
            binding.textViewTitle.text = "Report ID: ${report.id}"
            binding.textViewContent.text = report.text
        } else {
            binding.textViewContent.text = "Report not found."
        }
    }

    private fun viewPdfFromObjectBox(pdfId: Long) {
        try {
            val pdfBox = ObjectBox.store.boxFor(HealthReport::class.java)
            val pdfEntity = pdfBox.get(pdfId)

            if (pdfEntity?.pdfData != null) {
                // Save the PDF data to a temporary file
                val tempFile = File(this.cacheDir, "${pdfEntity.id}.pdf")
                tempFile.writeBytes(pdfEntity.pdfData!!)

                // Open the PDF using an Intent
                val pdfUri = FileProvider.getUriForFile(
                    this,
                    "${this.packageName}.provider",
                    tempFile
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(pdfUri, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } else {
                Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to view PDF", Toast.LENGTH_SHORT).show()
        }
    }

}

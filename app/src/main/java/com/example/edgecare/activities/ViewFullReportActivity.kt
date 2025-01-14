package com.example.edgecare.activities

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            binding.textViewContent.visibility= View.GONE
            binding.textViewTitle.text = "Report ID: ${report.id}"
        } else if (report != null) {
            binding.textViewContent.visibility= View.VISIBLE
            binding.textViewTitle.text = "Report ID: ${report.id}"
            binding.textViewContent.text = report.text
        } else {
            binding.textViewContent.visibility= View.VISIBLE
            binding.textViewContent.text = "Report not found."
        }
    }

    private fun viewPdfFromObjectBox(pdfId: Long) {
        try {
            val pdfEntity = healthReportBox.get(pdfId)
            if (pdfEntity?.pdfData != null) {
                // Save the PDF data to a temporary file
                val tempFile = File(this.cacheDir, "${pdfEntity.id}.pdf")
                tempFile.writeBytes(pdfEntity.pdfData!!)

                renderAllPdfPages(tempFile)
            } else {
                Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to view PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderAllPdfPages(file: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            // Clear the container
            binding.linearLayoutPdfContainer.removeAllViews()

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)

                // Render the page into a bitmap
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Create an ImageView to display the bitmap
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setImageBitmap(bitmap)
                    adjustViewBounds = true
                }

                // Add the ImageView to the container
                binding.linearLayoutPdfContainer.addView(imageView)

                // Close the page
                page.close()
            }

            // Close resources
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to render PDF pages", Toast.LENGTH_SHORT).show()
        }
    }
}

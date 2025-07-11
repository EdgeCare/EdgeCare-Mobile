package com.example.edgecare.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edgecare.ObjectBox
import com.example.edgecare.activities.ViewFullReportActivity
import com.example.edgecare.adapters.HealthReportAdapter
import com.example.edgecare.api.getReportAnalysis
import com.example.edgecare.databinding.ActivityReportHandleBinding
import com.example.edgecare.models.HealthReport
import com.example.edgecare.models.HealthReportChunk
import com.example.edgecare.models.HealthReportChunk_
import com.example.edgecare.utils.CryptoHelper
import com.example.edgecare.utils.EmbeddingUtils
import com.example.edgecare.utils.FileUtils
import io.objectbox.Box
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import java.io.InputStream

class ReportHandleFragment : Fragment() {
    private var _binding: ActivityReportHandleBinding? = null
    private val binding get() = _binding!!
    private lateinit var healthReportBox: Box<HealthReport>
    private lateinit var healthReportChunkBox: Box<HealthReportChunk>
    private lateinit var adapter: HealthReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityReportHandleBinding.inflate(inflater, container, false)
        val view = binding.root

        healthReportBox = ObjectBox.store.boxFor(HealthReport::class.java)
        healthReportChunkBox = ObjectBox.store.boxFor(HealthReportChunk::class.java)

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
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "application/pdf"))
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
            val mimeType = requireContext().contentResolver.getType(fileUri)
            var text = when (mimeType) {
                "text/plain" -> FileUtils.readTextFile(requireContext().contentResolver, fileUri)
                "application/pdf" -> extractTextFromPdf(fileUri)
                else -> null
            }

            var report = HealthReport()
            if(mimeType=="application/pdf" ) {
                if(text.isNullOrEmpty()){
                    text = "Can't extract content from pdf"
                }
                val inputStream = requireContext().contentResolver.openInputStream(fileUri)
                val pdfData = inputStream?.use { it.readBytes() }

                if (pdfData != null) {
                    // Save the PDF in ObjectBox
                    report = HealthReport(isPDF = true, text = CryptoHelper.encrypt(text), pdfData = pdfData)
                    generateHealthReportSummary(text){summary ->
                        report.summary = summary
                        healthReportBox.put(report)
                        saveHealthReportChunks(text, report.id)
                        loadHealthReports()
                        Toast.makeText(requireContext(), "Health report saved successfully", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            if (!text.isNullOrEmpty()) {
                if(mimeType!="application/pdf" ) {
                    report = HealthReport(text = CryptoHelper.encrypt(text))
                    generateHealthReportSummary(text){summary ->
                        report.summary = summary
                        healthReportBox.put(report)
                        saveHealthReportChunks(text, report.id)
                        loadHealthReports()
                        Toast.makeText(requireContext(), "Health report saved successfully", Toast.LENGTH_SHORT).show()
                    }
                }

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
        adapter = HealthReportAdapter (
            onViewClick = { report ->
                // Handle item click
                val intent = Intent(requireContext(), ViewFullReportActivity::class.java)
                intent.putExtra("report_id", report.id)
                startActivity(intent)
            },
            onDeleteClick = { report ->
                deleteHealthReport(report.id)
            },
            onSummaryClick = { report ->
                showReportSummary(report.summary)
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun extractTextFromPdf(fileUri: Uri): String {
        val text = StringBuilder()
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(fileUri)
            inputStream?.use { stream ->
                PdfReader(stream).use { reader ->
                    PdfDocument(reader).use { pdfDoc ->
                        for (i in 1..pdfDoc.numberOfPages) {
                            val pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i))
                            text.append(pageText).append("\n")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return text.toString()
    }


    private fun loadHealthReports() {
        val reports = healthReportBox.all.map { report ->
            val decryptedText = try {
                CryptoHelper.decrypt(report.text)
            } catch (e: Exception) {
                "[Decryption Failed]"
            }
            report.copy(text = decryptedText)
        }

        if(reports.size>0){
            setupRecyclerView()
            adapter.submitList(reports)
            binding.linearLayout2.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
        else {
            binding.recyclerView.visibility = View.GONE
            binding.linearLayout2.visibility = View.VISIBLE
        }
    }

    private fun deleteHealthReport(reportID:Long){

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Report")
            setMessage("Are you sure you want to delete report ID: $reportID?")

            setPositiveButton("Delete") { dialog, _ ->
                // Delete the report if user confirms
                healthReportBox.remove(reportID)
                val query = healthReportChunkBox.query(HealthReportChunk_.reportId.equal(reportID)).build()
                query.remove()
                loadHealthReports()
                Toast.makeText(requireContext(), "Report $reportID deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                // Cancel action
                Toast.makeText(requireContext(), "Delete canceled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            create()
            show()
        }
    }

    private fun showReportSummary(summary:String){
        println("Report summary")
        println(summary)
        showFormattedReportSummaryPopup(requireContext(),summary)
    }

    private fun generateHealthReportSummary(text: String, callback: (String) -> Unit) {
        context?.let { ctx ->
            getReportAnalysis(text, ctx) { response ->
                if (response != null) {
                    callback(response.reportSummary)
                } else {
                    callback("Error")
                }
            }
        } ?: run {
            callback("Error: Context is null")
        }
    }

    private fun showFormattedReportSummaryPopup(context: Context, rawSummary: String) {

        // Convert **bold** sections to actual bold text using SpannableStringBuilder
        val formattedSummary = formatSummaryWithBold(rawSummary)

        val textView = TextView(context).apply {
            text = formattedSummary
            setPadding(32, 32, 32, 32)
            textSize = 16f
        }

        val scrollView = ScrollView(context).apply {
            addView(textView)
        }

        AlertDialog.Builder(context)
            .setTitle("Health Report Summary")
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show()
    }

    fun formatSummaryWithBold(summary: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()

        // Split summary by lines to apply bullet points
        val lines = summary.lines()

        for (line in lines) {
            if (line.isBlank()) continue

            spannable.append("")

            var startIndex = 0
            var workingLine = line

            while (true) {
                val boldStart = workingLine.indexOf("**", startIndex)
                if (boldStart == -1) {
                    spannable.append(workingLine.substring(startIndex))
                    break
                }

                val boldEnd = workingLine.indexOf("**", boldStart + 2)
                if (boldEnd == -1) {
                    // No matching end marker, treat as normal text
                    spannable.append(workingLine.substring(startIndex))
                    break
                }

                // Append text before bold
                spannable.append(workingLine.substring(startIndex, boldStart))

                // Append bold text
                val boldText = workingLine.substring(boldStart + 2, boldEnd)
                val boldStartInSpannable = spannable.length
                spannable.append(boldText)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    boldStartInSpannable,
                    boldStartInSpannable + boldText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                startIndex = boldEnd + 2
            }

            spannable.append("\n")
        }

        return spannable
    }




    private fun saveHealthReportChunks(text: String, reportId:Long){
        val chunkSize = 50
        val overlap = 10
        val chunkEmbeddings = generateChunkEmbeddingsWithOverlap(text, chunkSize, overlap)
        val healthReportChunkBox = ObjectBox.store.boxFor(HealthReportChunk::class.java)
        saveChunkEmbeddingsWithOverlap(reportId, chunkEmbeddings, healthReportChunkBox)
    }

    private fun chunkTextWithOverlap(text: String, chunkSize: Int = 50, overlap: Int = 10): List<String> {
        // Split the text into words
        val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        val chunks = mutableListOf<String>()
        var startIndex = 0

        while (startIndex < words.size) {
            // Get the chunk
            val endIndex = (startIndex + chunkSize).coerceAtMost(words.size)
            val chunk = words.subList(startIndex, endIndex).joinToString(" ")

            chunks.add(chunk)

            // Move to the next chunk start position with overlap
            startIndex += (chunkSize - overlap).coerceAtLeast(1)
        }

        return chunks
    }

    private fun generateChunkEmbeddingsWithOverlap(report: String, chunkSize: Int = 50, overlap: Int = 10): Map<String, FloatArray> {
        val chunks = chunkTextWithOverlap(report, chunkSize, overlap)
        val embeddings = mutableMapOf<String, FloatArray>()

        chunks.forEach { chunk ->
            val embedding = EmbeddingUtils.computeEmbedding(chunk, requireContext())
            if (embedding != null) {
                embeddings[chunk] = embedding
            }
        }

        return embeddings
    }

    private fun saveChunkEmbeddingsWithOverlap(
        reportId: Long,
        chunkEmbeddings: Map<String, FloatArray>,
        healthReportChunkBox: Box<HealthReportChunk>
    ) {
        chunkEmbeddings.forEach { (chunk, embedding) ->
            val healthReportChunk = HealthReportChunk(reportId = reportId, text = chunk, embedding = embedding)
            healthReportChunkBox.put(healthReportChunk)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}
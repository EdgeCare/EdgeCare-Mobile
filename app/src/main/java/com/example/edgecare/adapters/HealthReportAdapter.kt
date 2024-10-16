package com.example.edgecare.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edgecare.models.HealthReport
import com.example.edgecare.databinding.ItemHealthReportBinding

class HealthReportAdapter(
    private val onItemClick: (HealthReport) -> Unit
) : RecyclerView.Adapter<HealthReportAdapter.HealthReportViewHolder>() {

    private var reports: List<HealthReport> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthReportViewHolder {
        val binding = ItemHealthReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int = reports.size

    fun submitList(reports: List<HealthReport>) {
        this.reports = reports
        notifyDataSetChanged()
    }

    inner class HealthReportViewHolder(private val binding: ItemHealthReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: HealthReport) {
            binding.textViewTitle.text = "Report ID: ${report.id}"
            binding.textViewSnippet.text = report.text.take(100) // Show first 100 chars as snippet

            binding.root.setOnClickListener {
                onItemClick(report)
            }
        }
    }
}

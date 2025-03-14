package com.example.edgecare.utils

import android.util.Log

object LatencyLogger {
    private val timestamps = mutableMapOf<String, Long>()
    private val latencyRecords = mutableMapOf<String, MutableList<Long>>()

    // Start time logging
    fun start(tag: String) {
        timestamps[tag] = System.currentTimeMillis()
    }

    // End time logging & save the result
    fun end(tag: String) {
        val startTime = timestamps[tag]
        if (startTime != null) {
            val latency = System.currentTimeMillis() - startTime
            latencyRecords.getOrPut(tag) { mutableListOf() }.add(latency)
            Log.d("LatencyLogger", "$tag took $latency ms")
        } else {
            Log.d("LatencyLogger", "Start time for $tag not found")
        }
    }

    // Compute Mean Latency
    private fun getMeanLatency(tag: String): Double {
        val records = latencyRecords[tag]
        return if (records.isNullOrEmpty()) 0.0 else records.average()
    }

    // Compute Standard Deviation
    private fun getStandardDeviation(tag: String): Double {
        val records = latencyRecords[tag]
        if (records.isNullOrEmpty()) return 0.0
        val mean = records.average()
        val variance = records.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }

    // Log Final Summary for Multiple Runs
    fun logSummary() {
        latencyRecords.keys.forEach { tag ->
            val mean = getMeanLatency(tag)
            val stdDev = getStandardDeviation(tag)
            Log.d("LatencyLogger", "Summary for $tag - Mean: ${mean}ms, Std Dev: ${stdDev}ms")
        }
    }
}

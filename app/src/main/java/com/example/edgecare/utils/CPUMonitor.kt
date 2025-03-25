package com.example.edgecare.utils

import android.os.Debug
import android.os.SystemClock
import android.util.Log

object CPUMonitor {
    private var lastAppCpuTime: Long = 0
    private var lastUptimeMillis: Long = 0

    private val usageHistory = mutableMapOf<String, MutableList<Double>>()

    /**
     * Gets the CPU usage of the app as a percentage.
     */
    fun getCPUUsage(): Double {
        val appCpuTime = Debug.threadCpuTimeNanos() / 1_000_000 // Convert nanoseconds to milliseconds
        val uptimeMillis = SystemClock.elapsedRealtime()

        if (lastAppCpuTime == 0L || lastUptimeMillis == 0L) {
            lastAppCpuTime = appCpuTime
            lastUptimeMillis = uptimeMillis
            return 0.0
        }

        val appDelta = appCpuTime - lastAppCpuTime
        val timeDelta = uptimeMillis - lastUptimeMillis

        lastAppCpuTime = appCpuTime
        lastUptimeMillis = uptimeMillis

        return if (timeDelta > 0) {
            (100.0 * appDelta) / timeDelta
        } else {
            0.0
        }
    }

    /**
     * Logs the CPU usage for a specific process stage.
     */
    fun logCPUUsage(stage: String) {
        val usage = getCPUUsage()
        usageHistory.getOrPut(stage) { mutableListOf() }.add(usage)
        Log.d("CPUMonitor", "$stage CPU Usage: $usage%")
    }

    /**
     * Logs the average CPU usage for all recorded stages.
     */
    fun logAverageUsage() {
        for ((stage, usages) in usageHistory) {
            if (usages.isNotEmpty()) {
                val average = usages.average()
                Log.d("CPUMonitor", "Average CPU Usage for $stage: ${String.format("%.2f", average)}%")
            }
        }
    }

    /**
     * Clears all recorded CPU usage history.
     */
    fun resetUsageData() {
        usageHistory.clear()
        lastAppCpuTime = 0
        lastUptimeMillis = 0
        Log.d("CPUMonitor", "CPU usage data has been reset.")
    }
}

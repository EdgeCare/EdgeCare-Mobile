package com.example.edgecare.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.Log
import androidx.annotation.RequiresApi

object RAMMonitor {
    private val usageHistory = mutableMapOf<String, MutableList<Double>>()

    /**
     * Logs app-specific memory usage at a given stage.
     */
    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    fun logRAMUsage(context: Context, stage: String) {
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)

        val totalPss = memoryInfo.totalPss // in KB
        val privateDirty = memoryInfo.totalPrivateDirty
        val sharedDirty = memoryInfo.totalSharedDirty

        val ramInMB = totalPss / 1024.0
        usageHistory.getOrPut(stage) { mutableListOf() }.add(ramInMB)

        Log.d("RAMMonitor", "$stage - RAM Used: ${"%.2f".format(ramInMB)} MB (PSS)")
        Log.d("RAMMonitor", "$stage - Private Dirty: $privateDirty KB, Shared Dirty: $sharedDirty KB")
    }

    /**
     * Logs average RAM usage per stage.
     */
    fun logAverageRAMUsage() {
        for ((stage, usages) in usageHistory) {
            if (usages.isNotEmpty()) {
                val average = usages.average()
                val peak = usages.maxOrNull() ?: 0.0
                Log.d("RAMMonitor", "Average RAM Usage for $stage: ${"%.2f".format(average)} MB")
                Log.d("RAMMonitor", "Peak RAM Usage for $stage: ${"%.2f".format(peak)} MB")
            }
        }
    }

    /**
     * Clears all stored memory usage data.
     */
    fun resetUsageData() {
        usageHistory.clear()
        Log.d("RAMMonitor", "RAM usage data has been reset.")
    }

    /**
     * Optionally log system-wide memory info (not just your app).
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun logSystemRAM(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val availMemMB = memInfo.availMem / 1048576L
        val totalMemMB = memInfo.totalMem / 1048576L
        val usedMemMB = totalMemMB - availMemMB

        Log.d("RAMMonitor", "System RAM - Used: ${usedMemMB} MB / ${totalMemMB} MB")
    }
}

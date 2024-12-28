package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.Calendar

object UsageStatsHelper {

    fun getAppUsageStats(context: Context, resetTimestamp: Long): List<UsageStats> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()

            // Use the reset timestamp as the start time
            val startTime = resetTimestamp
            val endTime = now

            Log.d("UsageStatsHelper", "Querying usage stats from start time: ${startTime} (${formatTimestamp(startTime)}) to end time: ${endTime} (${formatTimestamp(endTime)})")

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val filteredUsageStats = usageStats.filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }

            return filteredUsageStats
        } else {
            return emptyList()
        }
    }

    fun getTopUsedApps(context: Context): List<Pair<String, Long>> {
        val resetTimestamp = (context as LockScreenActivity).getResetTimestamp()
        val usageStatsList = getAppUsageStats(context, resetTimestamp)
        val appUsageMap = mutableMapOf<String, Long>()

        for (usageStat in usageStatsList) {
            val packageName = usageStat.packageName
            val totalTimeInForeground = usageStat.totalTimeInForeground

            if (totalTimeInForeground > 0) {
                appUsageMap[packageName] = totalTimeInForeground
            }
        }

        return appUsageMap.toList().sortedByDescending { it.second }
    }


    fun getTopUsedAppsSinceReset(context: Context): List<Pair<String, Long>> {
        val resetTimestamp = (context as LockScreenActivity).getResetTimestamp()
        val usageStatsList = getAppUsageStats(context, resetTimestamp)
        val appUsageMap = mutableMapOf<String, Long>()

        // Read the baseline usage times from SharedPreferences
        val sharedPreferences = context.getSharedPreferences(LockScreenActivity.PREFS_NAME, Context.MODE_PRIVATE)

        for (usageStat in usageStatsList) {
            val packageName = usageStat.packageName
            val totalTimeInForeground = usageStat.totalTimeInForeground

            // Get the baseline usage recorded at the reset time
            val baselineUsage = sharedPreferences.getLong("baseline_$packageName", 0L)

            // Calculate the usage since the reset
            val usageSinceReset = totalTimeInForeground - baselineUsage
            if (usageSinceReset > 0) {
                appUsageMap[packageName] = usageSinceReset
            }
        }

        return appUsageMap.toList().sortedByDescending { it.second }
    }

    // Utility function to format timestamp for better readability
    private fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", calendar).toString()
    }
}

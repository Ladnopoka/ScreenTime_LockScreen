package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.BoxScope
import java.util.Calendar

object UsageStatsHelper {

    private fun getAppUsageStats(context: Context): List<UsageStats> {
        // Check if the API level is at least 22 before accessing UsageStatsManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Set up a calendar for the time range (start from 6 AM today)
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 6) // Set hour to 6 AM
            calendar.set(Calendar.MINUTE, 0) // Set minutes to 0
            calendar.set(Calendar.SECOND, 0) // Set seconds to 0
            calendar.set(Calendar.MILLISECOND, 0) // Set milliseconds to 0
            val startTime = calendar.timeInMillis

            // End time is the current time
            val endTime = System.currentTimeMillis()

            // Fetch usage stats from 6 AM onwards
            return usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } else {
            // If API level is below 22, return an empty list
            return emptyList()
        }
    }

    fun getTopUsedApps(context: Context): List<Pair<String, Long>> {
        val usageStatsList = getAppUsageStats(context)
        val appUsageMap = mutableMapOf<String, Long>()

        for (usageStat in usageStatsList) {
            val packageName = usageStat.packageName
            val totalTimeInForeground = usageStat.totalTimeInForeground

            // Only consider apps that were used
            if (totalTimeInForeground > 0) {
                appUsageMap[packageName] = totalTimeInForeground
            }
        }

        // Sort the map by usage time in descending order
        return appUsageMap.toList().sortedByDescending { it.second }
    }
}

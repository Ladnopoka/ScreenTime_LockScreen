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

            // Set up a calendar for the time range (e.g., the past day)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1) // Get stats for the last 24 hours

            // Fetch usage stats
            return usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                calendar.timeInMillis,
                System.currentTimeMillis()
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

package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.Calendar

object UsageStatsHelper {

    private fun getAppUsageStats(context: Context): List<UsageStats> {
        // Check if the API level is at least 22 before accessing UsageStatsManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Get the current time
            val now = System.currentTimeMillis()

            // Set up a calendar for 6 AM today
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, 6)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Determine start time
            val startTime = if (now >= calendar.timeInMillis) {
                // If it's after 6 AM, use today's 6 AM
                calendar.timeInMillis
            } else {
                // Otherwise, use 6 AM from the previous day
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }

            // End time is the current time
            val endTime = now

            // Fetch usage stats for the time range
            val usageStats =  usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            // Log each app's usage stats for debugging
            val filteredUsageStats = usageStats.filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }

            for (usageStat in filteredUsageStats) {
                val totalTimeInMillis = usageStat.totalTimeInForeground
                val hours = totalTimeInMillis / (1000 * 60 * 60)
                val minutes = (totalTimeInMillis % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (totalTimeInMillis % (1000 * 60)) / 1000

                Log.d(
                    "UsageStatsFiltered",
                    "Package: ${usageStat.packageName}, Time: ${hours}h ${minutes}m ${seconds}s"
                )
            }

            return usageStats
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

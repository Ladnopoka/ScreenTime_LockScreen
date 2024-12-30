package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.Calendar

object UsageStatsHelper {

    private fun getAppUsageStats(context: Context): List<UsageStats> {
        // Check if the API level is at least 22 before accessing UsageStatsManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Get the current time
            val now = System.currentTimeMillis()

            // Calculate start time (24 hours ago)
            val startTime = now - (24 * 60 * 60 * 1000) // 24 hours in milliseconds

            // Fetch usage stats for the time range
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                now
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

            return filteredUsageStats
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun getWeeklyAppUsage(context: Context): Map<String, Float> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()

        // End time: Now
        val endTime = System.currentTimeMillis()

        // Start time: 7 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis

        // Query usage stats
        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        // Initialize map for daily totals
        val dailyUsageMap = mutableMapOf<String, Float>()
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        // Initialize with 0 for each day
        for (day in daysOfWeek) {
            dailyUsageMap[day] = 0f
        }

        // Process usage stats
        for (usageStat in usageStatsList) {
            val totalTimeInForeground = usageStat.totalTimeInForeground
            val timestamp = usageStat.firstTimeStamp

            // Get the day of the week
            val dayOfWeek = getDayOfWeek(timestamp)

            if (dayOfWeek in daysOfWeek) {
                val hours = totalTimeInForeground / (1000 * 60 * 60).toFloat() // Convert to hours
                dailyUsageMap[dayOfWeek] = dailyUsageMap.getOrDefault(dayOfWeek, 0f) + hours
            }
        }

        return dailyUsageMap
    }

    // Helper function to get day of the week
    private fun getDayOfWeek(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }

}


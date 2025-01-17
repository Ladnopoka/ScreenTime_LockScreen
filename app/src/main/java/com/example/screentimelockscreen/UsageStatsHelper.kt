package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.Calendar

data class AppUsageInfo(
    val appName: String,
    val usageTime: Long,
    val appIcon: android.graphics.drawable.Drawable
)

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

    fun getTopUsedApps(context: Context): List<AppUsageInfo> {
        val usageStatsList = getAppUsageStats(context)
        val packageManager = context.packageManager
        val appUsageList = mutableListOf<AppUsageInfo>()

        for (usageStat in usageStatsList) {
            try {
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(usageStat.packageName, 0)
                ).toString()
                val appIcon = packageManager.getApplicationIcon(usageStat.packageName)
                val usageTime = usageStat.totalTimeInForeground

                // Only add apps with usage time greater than 0
                if (usageTime > 0) {
                    appUsageList.add(AppUsageInfo(appName, usageTime, appIcon))
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore apps that are not found
            }
        }

        // Sort apps by usage time in descending order
        return appUsageList.sortedByDescending { it.usageTime }
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


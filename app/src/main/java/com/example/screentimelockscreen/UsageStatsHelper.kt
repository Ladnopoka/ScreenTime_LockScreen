package com.example.screentimelockscreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.BoxScope
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
}

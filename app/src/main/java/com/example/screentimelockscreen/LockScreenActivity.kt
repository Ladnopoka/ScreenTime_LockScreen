package com.example.screentimelockscreen

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.screentimelockscreen.ui.theme.ScreenTimeLockScreenTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar

class LockScreenActivity : ComponentActivity() {
    private val appUsageData = mutableStateOf<List<Pair<String, Long>>>(emptyList())

    companion object {
        const val PREFS_NAME = "ScreenTimeLockScreenPrefs"
    }
    private val KEY_RESET_TIMESTAMP = "reset_timestamp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScreenTimeLockScreenTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // UI Components (dynamic background, etc.)
                    val images = listOf(
                        R.drawable.background1,
                        R.drawable.background2,
                        R.drawable.background3
                    )
                    val randomImage = remember { images[Random.nextInt(images.size)] }

                    Image(
                        painter = painterResource(id = randomImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Display app usage data
                    AppUsageDisplay()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update app usage data every time the activity resumes
        refreshAppUsageData()
    }

    private fun refreshAppUsageData() {
        // Fetch the latest app usage data
        //appUsageData.value = UsageStatsHelper.getTopUsedApps(this)
        appUsageData.value = UsageStatsHelper.getTopUsedAppsSinceReset(this)


        // Log for debugging
        Log.d("LockScreenActivity", "App usage data refreshed: ${appUsageData.value}")
    }

    @Composable
    fun AppUsageDisplay() {
        val usageList = appUsageData.value // Observe changes in app usage data

        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = buildString {
                    append("App Usage since 6AM:\n\n")
                    for ((packageName, time) in usageList) {
                        val hours = time / (1000 * 60 * 60)
                        val minutes = (time % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (time % (1000 * 60)) / 1000

                        // Build the time string dynamically
                        val timeString = buildString {
                            if (hours > 0) append("${hours}h ")
                            if (minutes > 0 || hours > 0) append("${minutes}m ")
                            append("${seconds}s")
                        }

                        append("$packageName: $timeString\n")
                    }
                },
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    //.align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    ),
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Left
                )
            )

            // Reset Button
            Button(
                onClick = { resetAppUsage() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = "Reset Usage Stats")
            }
        }
    }

    fun getResetTimestamp(): Long {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = sharedPreferences.getLong(KEY_RESET_TIMESTAMP, 0)
        Log.d("LockScreenActivity", "Retrieved reset timestamp: ${formatTimestamp(timestamp)}")
        return timestamp
    }

    private fun setResetTimestamp(timestamp: Long) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong(KEY_RESET_TIMESTAMP, timestamp).apply()
        Log.d("LockScreenActivity", "Set reset timestamp: ${formatTimestamp(timestamp)}")
    }

    private fun resetAppUsage() {
        val currentTime = System.currentTimeMillis()
        setResetTimestamp(currentTime)

        // Ensure the timestamp is saved before refreshing
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putLong(KEY_RESET_TIMESTAMP, currentTime)
            apply() // Use apply() to save the changes asynchronously
        }

        // Clear accumulated usage times in SharedPreferences
        sharedPreferences.edit().clear().apply()

        refreshAppUsageData() // Refresh usage data only after timestamp is saved
    }

    fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", calendar).toString()
    }
}

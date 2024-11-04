package com.example.screentimelockscreen

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeLockScreenTheme {
                val appUsageData = UsageStatsHelper.getTopUsedApps(this)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = buildString {
                            append("App Usage for the last 24 hours:\n\n")
                            for ((packageName, time) in appUsageData) {
                                append("$packageName: ${time / 1000 / 60} minutes\n")
                            }
                        },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Fetch and update the app usage information when the activity resumes
        updateAppUsageData()
    }

    private fun updateAppUsageData() {
        // Code to fetch app usage data and update the UI
        Log.d("LockScreenActivity", "Updating app usage data")
        // TODO: Implement logic to fetch and display app usage data
    }
}

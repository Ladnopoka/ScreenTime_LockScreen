package com.example.screentimelockscreen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.screentimelockscreen.ui.theme.ScreenTimeLockScreenTheme

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LockScreenActivity", "LockScreenActivity launched")

        setContent {
            ScreenTimeLockScreenTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fetch and display app usage information
                    Greeting(name = "ScreenTime LockScreen")
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
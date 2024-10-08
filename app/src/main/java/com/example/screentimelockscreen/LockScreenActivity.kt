package com.example.screentimelockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.screentimelockscreen.ui.theme.ScreenTimeLockScreenTheme
import android.util.Log

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LockScreenActivity", "LockScreenActivity launched")

        setContent {
            ScreenTimeLockScreenTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Reuse the Greeting composable here to display the same content as MainActivity
                    Greeting(name = "ScreenTime LockScreen")
                }
            }
        }
    }
}
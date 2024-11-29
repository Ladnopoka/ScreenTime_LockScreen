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
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeLockScreenTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Random Background Image
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

                    // App Usage Data
                    val appUsageData = UsageStatsHelper.getTopUsedApps(this@LockScreenActivity)

                    Text(
                        text = buildString {
                            append("App Usage for the last 24 hours:\n\n")
                            for ((packageName, time) in appUsageData) {
                                append("$packageName: ${time / 1000 / 60} minutes\n")
                            }
                        },
                        fontSize = 16.sp,
                        color = Color.White, // Use white text for better visibility
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState())
                            //.background(Color(0x80000000)) // Semi-transparent black background
                            .padding(8.dp), // Padding inside the text block
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black, // Drop shadow color
                                offset = Offset(2f, 2f), // Shadow offset
                                blurRadius = 4f // Shadow blur radius
                            ),
                            textAlign = TextAlign.Left // Optional alignment
                        )
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

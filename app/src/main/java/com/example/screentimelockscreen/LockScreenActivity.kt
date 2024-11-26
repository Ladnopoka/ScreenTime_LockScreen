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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.screentimelockscreen.ui.theme.ScreenTimeLockScreenTheme
import com.example.screentimelockscreen.toBitmap
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Locale

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeLockScreenTheme {
                // App Usage Data
                val appUsageData: List<AppUsageInfo> = UsageStatsHelper.getTopUsedApps(this@LockScreenActivity)

                // Random Background Image
                val images = listOf(
                    R.drawable.background1,
                    R.drawable.background2,
                    R.drawable.background3
                )
                val randomImage = remember { images[Random.nextInt(images.size)] }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background Image
                    Image(
                        painter = painterResource(id = randomImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // App Usage List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Your App Usage Since 6AM:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp),
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                        items(appUsageData) { appUsageInfo ->
                            val usageSeconds = appUsageInfo.usageTime / 1000
                            val hours = usageSeconds / 3600
                            val minutes = (usageSeconds % 3600) / 60
                            val seconds = usageSeconds % 60

                            val displayText = buildString {
                                if (hours > 0) append("$hours hr")
                                if (minutes > 0) {
                                    if (isNotEmpty()) append(", ")
                                    append("$minutes min")
                                }
                                if (seconds > 0 || isEmpty()) {
                                    if (isNotEmpty()) append(", ")
                                    append("$seconds sec")
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App Icon
                                Image(
                                    bitmap = appUsageInfo.appIcon.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(end = 16.dp),
                                    contentScale = ContentScale.Crop
                                )

                                // App Name and Usage Time
                                Column {
                                    Text(
                                        text = appUsageInfo.appName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(1f, 1f),
                                                blurRadius = 2f
                                            )
                                        )
                                    )
                                    Text(
                                        text = displayText,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAppUsageData()
    }

    private fun updateAppUsageData() {
        Log.d("LockScreenActivity", "Updating app usage data")
    }
}

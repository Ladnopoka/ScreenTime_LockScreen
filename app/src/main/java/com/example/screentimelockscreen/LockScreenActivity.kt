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
import android.content.res.Resources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream

class LockScreenActivity : ComponentActivity() {
    private val appUsageData = mutableStateOf<List<Pair<String, Long>>>(emptyList())
    private var currentImageIndex = 0 // Keeps track of the current image index
    private var lastImageIndex = -1 // Tracks the last used image index


    // List of resource IDs for the photos in res/raw/personal_photos
    private val personalPhotoIds = listOf(
        R.raw.photo1, // Corresponding to res/raw/personal_photos/photo1.jpg
        R.raw.photo2,
        R.raw.photo3,
        R.raw.photo4,
        R.raw.photo5,
        R.raw.photo6,
        R.raw.photo7,
        R.raw.photo8,
        R.raw.photo9,
        R.raw.photo10,
        R.raw.photo11,
        R.raw.photo12,
        R.raw.photo13,
        R.raw.photo14,
        R.raw.photo15
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeLockScreenTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Display the current background image
                    val currentImageBitmap = loadImageFromRaw(
                        getRandomImageIndex()
                    )
                    if (currentImageBitmap != null) {
                        Image(
                            bitmap = currentImageBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Display app usage data
                    AppUsageDisplay()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update the current image index and wrap around if necessary
        //currentImageIndex = (currentImageIndex + 1) % personalPhotoIds.size
        // Update app usage data every time the activity resumes
        refreshAppUsageData()
    }

    private fun refreshAppUsageData() {
        // Fetch the latest app usage data
        appUsageData.value = UsageStatsHelper.getTopUsedApps(this)

        // Log for debugging
        Log.d("LockScreenActivity", "App usage data refreshed: ${appUsageData.value}")
    }

    // Helper function to load an image from the res/raw directory
    private fun loadImageFromRaw(resourceId: Int): ImageBitmap? {
        return try {
            val inputStream: InputStream = resources.openRawResource(resourceId)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e("LockScreenActivity", "Error loading image resource ID: $resourceId", e)
            null
        }
    }

    // Function to get a random image index, ensuring no immediate repetition
    private fun getRandomImageIndex(): Int {
        var newIndex: Int
        do {
            newIndex = Random.nextInt(personalPhotoIds.size)
        } while (newIndex == lastImageIndex) // Ensure it's not the same as the last image
        lastImageIndex = newIndex
        return personalPhotoIds[newIndex]
    }

    @Composable
    fun AppUsageDisplay() {
        val usageList = appUsageData.value // Observe changes in app usage data

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
    }
}

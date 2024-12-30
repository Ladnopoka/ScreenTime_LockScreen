package com.example.screentimelockscreen

import android.content.Context
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
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class LockScreenActivity : ComponentActivity() {
    private val appUsageData = mutableStateOf<List<Pair<String, Long>>>(emptyList())
    private var currentImageIndex = 0 // Keeps track of the current image index
    private var lastImageIndex = -1 // Tracks the last used image index
    private val currentImageBitmap = mutableStateOf<ImageBitmap?>(null)


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

    // Mutable queue for the shuffled image IDs
    private val imageQueue = mutableListOf<Int>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeLockScreenTheme {
                AppUsageScreen(this)
            }
        }

        // Refresh image and app usage data on creation
        // Initial setup
        reshuffleImages()
        refreshAppUsageData()
    }

    override fun onResume() {
        super.onResume()
        //refreshBackgroundImage()
        refreshAppUsageData()
    }

    override fun onRestart() {
        super.onRestart()
        //refreshBackgroundImage()
        refreshAppUsageData()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            Log.d("LockScreenActivityImg", "Windows Focus Changed")
            refreshBackgroundImage()
        }
    }

    private fun reshuffleImages() {
        imageQueue.clear()
        imageQueue.addAll(personalPhotoIds.shuffled())
        Log.d("LockScreenActivityImg", "Image queue reshuffled: $imageQueue")
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
            Log.e("LockScreenActivityImg", "Error loading image resource ID: $resourceId", e)
            null
        }
    }

    private fun refreshBackgroundImage() {
        // If the queue is empty, reshuffle the images
        if (imageQueue.isEmpty()) {
            reshuffleImages()
        }

        // Pop the next image from the queue
        val nextImage = imageQueue.removeAt(0)
        currentImageBitmap.value = loadImageFromRaw(nextImage)
        Log.d("LockScreenActivityImg", "Background image refreshed to ID: $nextImage")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    fun AppUsageDisplay() {
        val usageList = appUsageData.value // Observe changes in app usage data

        // Retrieve weekly data
        Log.d("UsageListSeeWhatsUP", "Usage List: $usageList")

        Text(
            text = buildString {
                append("24 Hour App Usage:\n\n")
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

    private fun prepareBarGraphData(weeklyUsageData: Map<String, Float>): BarData {
        val entries = mutableListOf<BarEntry>()
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        for ((index, day) in daysOfWeek.withIndex()) {
            val usageHours = weeklyUsageData[day] ?: 0f

            // Round fractional hours to the nearest minute
            val totalMinutes = (usageHours * 60).toInt() // Convert hours to minutes
            val roundedHours = totalMinutes / 60 // Integer hours
            val remainingMinutes = totalMinutes % 60 // Remaining minutes

            // Add entry to the bar graph using rounded hours and fractional minutes
            val roundedUsage = roundedHours + (remainingMinutes / 60f) // Convert minutes back to fractional hours
            entries.add(BarEntry(index.toFloat(), roundedUsage))
        }

        val dataSet = BarDataSet(entries, "Weekly App Usage (Hours)").apply {
            valueTextSize = 12f
            valueFormatter = TimeValueFormatter() // Use the custom formatter
        }

        return BarData(dataSet)
    }

    @Composable
    fun WeeklyUsageBarGraph(data: BarData) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Set height explicitly
            factory = { context ->
                BarChart(context).apply {
                    this.data = data

                    // Configure chart description
                    description.isEnabled = false
                    legend.isEnabled = false

                    // X-axis styling
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        )
                        granularity = 1f
                        position = XAxis.XAxisPosition.BOTTOM
                        textSize = 12f
                        textColor = android.graphics.Color.WHITE // Change text color to white
                    }

                    // Y-axis styling (Left)
                    axisLeft.apply {
                        textSize = 12f
                        textColor = android.graphics.Color.WHITE // Change text color to white
                    }

                    // Disable right Y-axis
                    axisRight.isEnabled = false

                    // Customize bar value text
                    data.setValueTextSize(12f)
                    data.setValueTextColor(android.graphics.Color.WHITE) // Change text color to white

                    // Shadow effect for text (Optional, requires custom logic in MPAndroidChart)
                    // Note: MPAndroidChart does not natively support shadow styling for text.
                    // You would need to override drawing methods for advanced shadow effects.

                    // Refresh chart
                    invalidate()
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    fun AppUsageScreen(context: Context) {
        val weeklyUsageData = remember { UsageStatsHelper.getWeeklyAppUsage(context) }
        val barData = remember { prepareBarGraphData(weeklyUsageData) }
        val currentImageBitmapState = remember { mutableStateOf(currentImageBitmap.value) }

        // Observe changes to currentImageBitmap
        currentImageBitmap.value?.let { currentImageBitmapState.value = it }

        Box(modifier = Modifier.fillMaxSize()) {
            // Display background image
            currentImageBitmapState.value?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Frame for Bar Graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(width = 2.dp, color = Color.Gray)
                        .padding(8.dp)
                ) {
                    WeeklyUsageBarGraph(data = barData)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Frame for App Stats
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(width = 2.dp, color = Color.Gray)
                        .padding(8.dp)
                ) {
                    AppUsageDisplay()
                }
            }
        }
    }
}

class TimeValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val totalMinutes = (value * 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }
}

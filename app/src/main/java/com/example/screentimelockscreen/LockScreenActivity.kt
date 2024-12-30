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
//                Box(modifier = Modifier.fillMaxSize()) {
//                    val bitmap = currentImageBitmap.value
//                    if (bitmap != null) {
//                        Image(
//                            bitmap = bitmap,
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxSize(),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//
//                    // Display app usage data
//                    AppUsageDisplay()
//                }
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
            //refreshBackgroundImage()
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

    // Function to get a random image index, ensuring no immediate repetition
    private fun getRandomImageIndex(): Int {
        var newIndex: Int
        do {
            newIndex = Random.nextInt(personalPhotoIds.size)
        } while (newIndex == lastImageIndex)
        lastImageIndex = newIndex
        return newIndex
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
        val context = LocalContext.current
        val weeklyUsageData = remember { UsageStatsHelper.getWeeklyAppUsage(context) }
        val barData = remember { prepareBarGraphData(weeklyUsageData) }
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
            entries.add(BarEntry(index.toFloat(), usageHours))
        }

        val dataSet = BarDataSet(entries, "Weekly App Usage (Hours)").apply {
            valueTextSize = 12f
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
                    description.isEnabled = false
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        )
                        granularity = 1f
                        position = XAxis.XAxisPosition.BOTTOM
                        textSize = 12f
                    }
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    invalidate() // Refresh chart
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    fun AppUsageScreen(context: Context) {
        val weeklyUsageData = remember { UsageStatsHelper.getWeeklyAppUsage(context) }
        val barData = remember { prepareBarGraphData(weeklyUsageData) }
        val currentImageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

        // Load the background image
        currentImageBitmap.value = loadImageFromRaw(context)

        Box(modifier = Modifier.fillMaxSize()) {
            // Display background image
            currentImageBitmap.value?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Bar graph
                WeeklyUsageBarGraph(data = barData)

                Spacer(modifier = Modifier.height(16.dp))

                // App usage stats
                AppUsageDisplay()
            }
        }
    }


    private fun loadImageFromRaw(context: Context): ImageBitmap? {
        val resourceId = R.raw.photo1 // Replace with logic to load desired image
        return try {
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e("ImageLoad", "Failed to load image: $resourceId", e)
            null
        }
    }
}

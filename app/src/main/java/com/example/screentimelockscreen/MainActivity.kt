package com.example.screentimelockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimelockscreen.ui.theme.ScreenTimeLockScreenTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlin.random.Random
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import android.app.AppOpsManager
import android.content.Context
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.widget.Toast
import java.util.Calendar

// Main entry point of the app
// Handles Permissions, Overlay Permissions, Usage Stats Permissions
// Starts LockScreenService
class MainActivity : ComponentActivity() {
    private val screenOnReceiver = ScreenOnReceiver()

    @RequiresApi(Build.VERSION_CODES.M)
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            Log.d("MainActivity", "Overlay permission granted.")
            // Permission granted, continue with setup
        } else {
            Log.d("MainActivity", "Overlay permission denied.")
            // Permission denied, handle the case
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            Log.d("MainActivity", "Requesting overlay permission.")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        requestUsageAccessPermission()
        if (!hasUsageAccessPermission(this)) {
            Log.d("MainActivity", "Requesting usage access permission")
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

        // Start the LockScreenService
        val serviceIntent = Intent(this, LockScreenService::class.java)
        startService(serviceIntent)

        setContent {
            ScreenTimeLockScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "ScreenTime LockScreen",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        fun onDestroy() {
            super.onDestroy()
            // Unregister the receiver when the activity is destroyed
            unregisterReceiver(screenOnReceiver)
        }
    }

    // Function to check if the app has usage access permission
    private fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccessPermission() {
        if (!hasUsageAccessPermission(this)) {
            // Show a Toast message explaining why the permission is needed
            Toast.makeText(
                this,
                "Please grant usage access permission for the app to track app usage.",
                Toast.LENGTH_LONG
            ).show()

            Log.d("MainActivity", "Requesting usage access permission")
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // List of drawable resource IDs
    val images = listOf(
        R.drawable.background1,
        R.drawable.background2,
        R.drawable.background3
    )

    // Select a random image
    val randomImage = remember { images[Random.nextInt(images.size)] }


    Box(
        modifier = Modifier
            .fillMaxSize() // This makes the Box fill the entire screen
            //.background(Color.Cyan) // Set the background color for the whole screen
    ) {
        // Set the randomly selected image as the background
        Image(
            painter = painterResource(id = randomImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ensures the image covers the entire screen
        )


        Text(
            text = "ScreenTime LockScreen",
            modifier = modifier
                .align(Alignment.Center) // Center the text within the Box
                .padding(16.dp) // Padding around the text
                .border(2.dp, Color.Black)
                .shadow(4.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScreenTimeLockScreenTheme {
        Greeting("Android")
    }
}
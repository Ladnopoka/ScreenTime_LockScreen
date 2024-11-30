package com.example.screentimelockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import android.app.AppOpsManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat

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
        } else {
            startLockScreenService()
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


        // Launch LockScreenActivity
        val lockScreenIntent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(lockScreenIntent)

        // Optional: Finish MainActivity so it doesn't stay in the activity stack
        finish()
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

    private fun startLockScreenService() {
        val serviceIntent = Intent(this, LockScreenService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        val lockScreenIntent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(lockScreenIntent)
        finish()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ScreenTimeLockScreenTheme {
//        Greeting("Android")
//    }
//}
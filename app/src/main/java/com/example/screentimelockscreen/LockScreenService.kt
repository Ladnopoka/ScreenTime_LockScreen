package com.example.screentimelockscreen

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

//This service ensures the app runs in the background and
//listens for screen-on events
class LockScreenService : Service() {
    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            Log.d("LockScreenService", "Received action: $action")

            if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_USER_PRESENT) {
                if (!keyguardManager.isKeyguardLocked || action == Intent.ACTION_USER_PRESENT) {
                    launchLockScreenActivity(context)
                } else {
                    Log.d("LockScreenService", "Phone is locked, not launching LockScreenActivity")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LockScreenService", "Service created")

        // Register the screen on receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenOnReceiver, filter)

        // Start the service as a foreground service
        startForegroundService()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "lockscreen_service_channel"
            val channelName = "LockScreen Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("ScreenTime LockScreen Service")
                .setContentText("Running to monitor screen events")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            startForeground(1, notification)
        }
    }

    private fun launchLockScreenActivity(context: Context) {
        val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(lockScreenIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LockScreenService", "Service destroyed")
        unregisterReceiver(screenOnReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

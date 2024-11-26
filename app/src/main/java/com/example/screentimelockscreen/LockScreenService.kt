package com.example.screentimelockscreen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
    private val screenOnReceiver = ScreenOnReceiver()

    override fun onCreate() {
        super.onCreate()
        Log.d("LockScreenService", "Service created")

        // Register the screen on receiver
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filter)

        // Start the service as a foreground service
        startForegroundService()
    }

    private fun startForegroundService() {
        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "lockscreen_service_channel"
            val channelName = "LockScreen Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LockScreenService", "Service destroyed")
        unregisterReceiver(screenOnReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We do not bind this service, so return null
        return null
    }
}
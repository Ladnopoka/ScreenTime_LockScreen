package com.example.screentimelockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

//Broadcast receiver that listens for screen-on events and launches LockScreenActivity
class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            Log.d("ScreenOnReceiver", "Screen turned on, launching LockScreenActivity")

            val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(lockScreenIntent)
        }
    }
}
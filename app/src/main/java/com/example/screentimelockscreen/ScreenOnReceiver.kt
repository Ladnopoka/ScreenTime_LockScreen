package com.example.screentimelockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenOnReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScreenOnReceiver", "Screen turned on, launching LockScreenActivity")

        // Launch LockScreenActivity
        val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(lockScreenIntent)
    }
}
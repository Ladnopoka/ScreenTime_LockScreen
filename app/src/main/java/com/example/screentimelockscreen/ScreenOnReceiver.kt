package com.example.screentimelockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

//Broadcast receiver that listens for screen-on events and launches LockScreenActivity
class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        Log.d("ScreenOnReceiver", "Received action: $action")

        if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_USER_PRESENT) {
            if (!keyguardManager.isKeyguardLocked || action == Intent.ACTION_USER_PRESENT) {
                launchLockScreenActivity(context)
            } else {
                Log.d("ScreenOnReceiver", "Phone is locked, not launching LockScreenActivity")
            }
        }
    }

    private fun launchLockScreenActivity(context: Context) {
        val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(lockScreenIntent)
    }
}
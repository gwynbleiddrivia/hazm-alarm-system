package com.dreyfus.hazm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "alarm"
        Log.d("Hazm", "AlarmReceiver FIRED for '$label' at ${System.currentTimeMillis()}")
    }
}

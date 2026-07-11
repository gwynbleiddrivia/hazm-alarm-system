package com.dreyfus.hazm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm, triggerAtMillis: Long) {
        // What fires when the alarm goes off → our trampoline receiver.
        val fireIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", alarm.label)
            putExtra("id", alarm.id)
        }
        val firePending = PendingIntent.getBroadcast(
            context,
            alarm.id,                                  // unique per alarm = no collisions
            fireIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // What the OS opens if the user taps the alarm-clock status-bar icon.
        val showPending = PendingIntent.getActivity(
            context,
            alarm.id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPending)
        alarmManager.setAlarmClock(info, firePending)
        Log.d("Hazm", "SCHEDULED '${alarm.label}' for $triggerAtMillis")
    }
}

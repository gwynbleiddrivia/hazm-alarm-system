package com.dreyfus.hazm

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.room.Room

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(this, AppDatabase::class.java, "hazm.db")
            .allowMainThreadQueries()   // TEMP: OK for this first step; we move DB work off the UI thread later
            .build()

        val dao = db.alarmDao()
        dao.insert(Alarm(hour = 4, minute = 30, label = "Fajr"))
        val alarms = dao.getAll()

        val view = TextView(this).apply {
            text = "Hazm — ${alarms.size} alarm(s) saved:\n" +
                    alarms.joinToString("\n") { "${it.label} @ ${it.hour}:${it.minute}" }
            textSize = 22f
        }
        setContentView(view)
    }
}

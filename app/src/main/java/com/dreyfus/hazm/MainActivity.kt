package com.dreyfus.hazm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.room.Room

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(this, AppDatabase::class.java, "hazm.db")
            .allowMainThreadQueries()   // TEMP: still on UI thread; move off later
            .build()

        val dao = db.alarmDao()
        dao.insert(Alarm(hour = 4, minute = 30, label = "Fajr"))
        val alarms = dao.getAll()

        val summary = "Hazm — ${alarms.size} alarm(s) saved:\n" +
                alarms.joinToString("\n") { "${it.label} @ ${it.hour}:${it.minute}" }

        setContent {
            HazmScreen(summary)
        }
    }
}

@Composable
fun HazmScreen(summary: String) {
    MaterialTheme {
        Surface {
            Text(text = summary)
        }
    }
}

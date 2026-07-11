package com.dreyfus.hazm

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = TextView(this).apply {
            text = "Hazm Alarm — skeleton alive"
            textSize = 22f
        }
        setContentView(label)
    }
}

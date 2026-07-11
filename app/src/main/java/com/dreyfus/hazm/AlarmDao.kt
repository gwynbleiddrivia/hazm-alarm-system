package com.dreyfus.hazm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: Alarm): Long   // returns the new row's id

    @Query("SELECT * FROM alarms")
    fun getAll(): List<Alarm>
}

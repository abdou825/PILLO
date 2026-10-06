package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_readings")
data class HealthReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 1L,
    val type: HealthReadingType,
    val value1: Float,
    val value2: Float? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

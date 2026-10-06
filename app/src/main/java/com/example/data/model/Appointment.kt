package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 1L,
    val title: String,
    val type: AppointmentType = AppointmentType.DOCTOR,
    val dateTime: Long, // timestamp in ms
    val location: String = "",
    val notes: String = "",
    val repeatRule: String = "NONE", // NONE, MONTHLY, EVERY_3_MONTHS, YEARLY
    val isCompleted: Boolean = false
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicineId", "scheduledTime"]),
        Index(value = ["profileId"])
    ]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 1L,
    val medicineId: Long,
    val scheduledTime: Long, // Epoch timestamp in ms for the exact scheduled dose
    val timeLabel: String = "", // e.g. "08:30"
    val status: DoseStatus = DoseStatus.PENDING,
    val actionTime: Long? = null // When the user took, skipped, etc.
)

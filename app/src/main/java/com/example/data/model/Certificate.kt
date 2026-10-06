package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 1L,
    val medicineName: String,
    val milestoneType: MilestoneType,
    val badgeTier: BadgeTier,
    val daysCount: Int,
    val adherencePercent: Int,
    val startDate: Long,
    val endDate: Long,
    val certificateNumber: Int,
    val hideMedicineName: Boolean = true
)

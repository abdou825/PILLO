package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 1L,
    val name: String,
    val type: MedicineType,
    val totalQuantity: Int,
    val remainingQuantity: Int,
    val doseAmount: String, // e.g. "1 قرص", "5 مل"
    val timesPerDay: Int, // 1 to 6
    val mealRelation: MealRelation,
    val customScheduleTimes: String, // Comma-separated times e.g. "07:30,19:30"
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: MedicineStatus = MedicineStatus.ACTIVE,
    val treatmentPeriodNumber: Int = 1,
    val periodStartDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val lowStockNotifiedDate: String? = null // For low-stock alert tracking
)

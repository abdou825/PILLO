package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AgeMode
import com.example.data.model.AppointmentType
import com.example.data.model.BadgeTier
import com.example.data.model.DoseStatus
import com.example.data.model.HealthReadingType
import com.example.data.model.MealRelation
import com.example.data.model.MedicineStatus
import com.example.data.model.MedicineType
import com.example.data.model.MilestoneType

class Converters {
    @TypeConverter
    fun fromMedicineType(value: MedicineType): String = value.name

    @TypeConverter
    fun toMedicineType(value: String): MedicineType =
        runCatching { MedicineType.valueOf(value) }.getOrDefault(MedicineType.PILLS)

    @TypeConverter
    fun fromMealRelation(value: MealRelation): String = value.name

    @TypeConverter
    fun toMealRelation(value: String): MealRelation =
        runCatching { MealRelation.valueOf(value) }.getOrDefault(MealRelation.ANY_TIME)

    @TypeConverter
    fun fromAgeMode(value: AgeMode): String = value.name

    @TypeConverter
    fun toAgeMode(value: String): AgeMode =
        runCatching { AgeMode.valueOf(value) }.getOrDefault(AgeMode.YOUNG_ADULTS)

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus =
        runCatching { DoseStatus.valueOf(value) }.getOrDefault(DoseStatus.PENDING)

    @TypeConverter
    fun fromMedicineStatus(value: MedicineStatus): String = value.name

    @TypeConverter
    fun toMedicineStatus(value: String): MedicineStatus =
        runCatching { MedicineStatus.valueOf(value) }.getOrDefault(MedicineStatus.ACTIVE)

    @TypeConverter
    fun fromMilestoneType(value: MilestoneType): String = value.name

    @TypeConverter
    fun toMilestoneType(value: String): MilestoneType =
        runCatching { MilestoneType.valueOf(value) }.getOrDefault(MilestoneType.DAY_7)

    @TypeConverter
    fun fromBadgeTier(value: BadgeTier): String = value.name

    @TypeConverter
    fun toBadgeTier(value: String): BadgeTier =
        runCatching { BadgeTier.valueOf(value) }.getOrDefault(BadgeTier.BRONZE)

    @TypeConverter
    fun fromHealthReadingType(value: HealthReadingType): String = value.name

    @TypeConverter
    fun toHealthReadingType(value: String): HealthReadingType =
        runCatching { HealthReadingType.valueOf(value) }.getOrDefault(HealthReadingType.BLOOD_PRESSURE)

    @TypeConverter
    fun fromAppointmentType(value: AppointmentType): String = value.name

    @TypeConverter
    fun toAppointmentType(value: String): AppointmentType =
        runCatching { AppointmentType.valueOf(value) }.getOrDefault(AppointmentType.DOCTOR)
}

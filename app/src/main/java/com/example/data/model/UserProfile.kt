package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 1L,
    val displayName: String = "أنا (المستخدم الرئيسي)",
    val ageMode: AgeMode = AgeMode.YOUNG_ADULTS,
    val wakeTime: String = "07:00",
    val sleepTime: String = "23:00",
    val breakfastTime: String = "08:00",
    val lunchTime: String = "14:00",
    val dinnerTime: String = "20:00",
    val bloodType: String = "",
    val allergies: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isOwner: Boolean = true,
    val avatarColor: Long = 0xFF0D9488,
    val parentPin: String = "1234",
    val alarmSoundIndex: Int = 0,
    val snoozeMinutes: Int = 10,
    val voiceAlertEnabled: Boolean = false,
    val persistentEmergencyNotification: Boolean = false,
    val isOnboarded: Boolean = false,
    val ramadanModeEnabled: Boolean = false,
    val suhoorTime: String = "03:30",
    val iftarTime: String = "18:15",
    val travelTimeZoneOffsetHours: Int = 0,
    val userEmail: String = "hero@pillo.app",
    val fatherPhone: String = "",
    val avatarId: String = "avatar_1",
    val isParentMode: Boolean = false,
    val linkedSonEmail: String = "",
    val googlePhotoUrl: String = "",
    val isGoogleLinked: Boolean = false
)

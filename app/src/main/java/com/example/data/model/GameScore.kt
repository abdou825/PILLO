package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val gameId: String,
    val bestScore: Int,
    val lastPlayedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_challenges", primaryKeys = ["profileId", "challengeDate"])
data class DailyChallengeProgress(
    val profileId: Long,
    val challengeDate: String, // e.g. "2026-10-06"
    val taskType: String,
    val titleArabic: String,
    val isCompleted: Boolean = false,
    val coinsReward: Int = 30
)

@Entity(tableName = "streak_info")
data class StreakInfo(
    @PrimaryKey val profileId: Long = 1L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastDoseDay: String = ""
)

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val badgeId: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val iconName: String,
    val earnedAt: Long = System.currentTimeMillis()
)

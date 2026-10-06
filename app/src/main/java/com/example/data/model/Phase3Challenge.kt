package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChallengeStatus(val labelArabic: String) {
    PENDING("في انتظار الموافقة ⏳"),
    ACTIVE("تحدي قائم ومستمر 🔥"),
    DECLINED("تم الرفض بأدب"),
    ENDED("انتهى التحدي 🏁"),
    CANCELLED("ملغي")
}

@Entity(tableName = "local_challenges")
data class LocalChallenge(
    @PrimaryKey val id: String,
    val profileId: Long = 1L,
    val opponentPublicId: String,
    val opponentNickname: String,
    val challengeType: String, // e.g. "أسبوع التزام كامل" or "أعلى نسبة التزام في 5 أيام"
    val durationDays: Int = 7,
    val status: String = ChallengeStatus.ACTIVE.name,
    val myCommitmentPercent: Int = 100,
    val opponentCommitmentPercent: Int = 95,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (7L * 24 * 3600 * 1000)
)

@Entity(tableName = "friends", primaryKeys = ["userId", "friendPublicId"])
data class FriendRecord(
    val userId: String,
    val friendPublicId: String,
    val nickname: String,
    val commitmentTier: String = "ملتزم ذهبي 🏅",
    val streak: Int = 5,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_users", primaryKeys = ["userId", "blockedPublicId"])
data class BlockedUser(
    val userId: String,
    val blockedPublicId: String,
    val blockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "caretaker_links")
data class CaretakerLink(
    @PrimaryKey val id: String,
    val patientPublicId: String,
    val caretakerPublicId: String,
    val patientNickname: String,
    val caretakerNickname: String,
    val isCaretakerOfPatient: Boolean, // true if I am the caretaker, false if I am being monitored
    val allowMissedAlerts: Boolean = true,
    val status: String = "ACTIVE",
    val updatedAt: Long = System.currentTimeMillis()
)

data class PublicProfile(
    val publicId: String,
    val nickname: String,
    val avatarId: String = "avatar_1",
    val streak: Int = 0,
    val badgeCount: Int = 1,
    val commitmentTier: String = "ملتزم برونزي",
    val isPublic: Boolean = true,
    val isMinor: Boolean = false,
    val parentConsent: Boolean = false,
    val showStreak: Boolean = true,
    val showBadges: Boolean = true,
    val showTier: Boolean = true
)

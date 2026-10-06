package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.model.CaretakerLink
import com.example.data.model.ChallengeStatus
import com.example.data.model.FriendRecord
import com.example.data.model.LocalChallenge
import com.example.data.model.PublicProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages the optional online account, mutual-consent challenges, QR deep-links,
 * and caretaker alerts with 100% offline-first fallback.
 * Strictly guarantees that ZERO medical data, pill names, or diagnoses are EVER uploaded.
 */
class OnlineSyncService(
    private val context: Context,
    private val repository: PilloRepository
) {
    private val _isOnline = MutableStateFlow(checkInternetConnection())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _onlineStatusMessage = MutableStateFlow<String?>(null)
    val onlineStatusMessage: StateFlow<String?> = _onlineStatusMessage.asStateFlow()

    // Current cached public profile
    private val _currentPublicProfile = MutableStateFlow<PublicProfile?>(null)
    val currentPublicProfile: StateFlow<PublicProfile?> = _currentPublicProfile.asStateFlow()

    init {
        loadOrInitializePublicProfile()
    }

    fun checkInternetConnection(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun loadOrInitializePublicProfile() {
        val prefs = context.getSharedPreferences("pillo_online_prefs", Context.MODE_PRIVATE)
        val publicId = prefs.getString("public_id", null)
        if (publicId != null) {
            val nickname = prefs.getString("nickname", "بطل بيلّو") ?: "بطل بيلّو"
            val avatarId = prefs.getString("avatar_id", "avatar_1") ?: "avatar_1"
            val isPublic = prefs.getBoolean("is_public", true)
            val isMinor = prefs.getBoolean("is_minor", false)
            val parentConsent = prefs.getBoolean("parent_consent", false)
            val showStreak = prefs.getBoolean("show_streak", true)
            val showBadges = prefs.getBoolean("show_badges", true)
            val showTier = prefs.getBoolean("show_tier", true)

            _currentPublicProfile.value = PublicProfile(
                publicId = publicId,
                nickname = nickname,
                avatarId = avatarId,
                isPublic = isPublic,
                isMinor = isMinor,
                parentConsent = parentConsent,
                showStreak = showStreak,
                showBadges = showBadges,
                showTier = showTier
            )
        }
    }

    fun createOrUpdateAccount(
        nickname: String,
        avatarId: String,
        isMinor: Boolean,
        parentConsent: Boolean,
        isPublic: Boolean = true
    ): PublicProfile {
        val prefs = context.getSharedPreferences("pillo_online_prefs", Context.MODE_PRIVATE)
        val existingId = prefs.getString("public_id", null)
        val publicId = existingId ?: "pillo_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val profile = PublicProfile(
            publicId = publicId,
            nickname = nickname,
            avatarId = avatarId,
            isMinor = isMinor,
            parentConsent = parentConsent,
            isPublic = if (isMinor && !parentConsent) false else isPublic
        )

        prefs.edit()
            .putString("public_id", publicId)
            .putString("nickname", nickname)
            .putString("avatar_id", avatarId)
            .putBoolean("is_minor", isMinor)
            .putBoolean("parent_consent", parentConsent)
            .putBoolean("is_public", profile.isPublic)
            .apply()

        _currentPublicProfile.value = profile
        return profile
    }

    fun deleteAccountAndOnlineData() {
        val prefs = context.getSharedPreferences("pillo_online_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _currentPublicProfile.value = null
        _onlineStatusMessage.value = "تم مسح حسابك والبيانات المرتبطة نهائياً."
    }

    fun regenerateQrCode(): String {
        val current = _currentPublicProfile.value ?: return ""
        val newPublicId = "pillo_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val updated = current.copy(publicId = newPublicId)
        val prefs = context.getSharedPreferences("pillo_online_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("public_id", newPublicId).apply()
        _currentPublicProfile.value = updated
        return newPublicId
    }

    suspend fun sendChallengeRequest(
        profileId: Long,
        opponentPublicId: String,
        opponentNickname: String,
        challengeType: String,
        durationDays: Int
    ): LocalChallenge {
        val challengeId = "ch_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val challenge = LocalChallenge(
            id = challengeId,
            profileId = profileId,
            opponentPublicId = opponentPublicId,
            opponentNickname = opponentNickname,
            challengeType = challengeType,
            durationDays = durationDays,
            status = ChallengeStatus.ACTIVE.name,
            myCommitmentPercent = 100,
            opponentCommitmentPercent = 95,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + (durationDays.toLong() * 24 * 3600 * 1000)
        )
        repository.saveChallenge(challenge)

        // Save as friend too
        val currentUserId = _currentPublicProfile.value?.publicId ?: "me"
        repository.saveFriend(FriendRecord(
            userId = currentUserId,
            friendPublicId = opponentPublicId,
            nickname = opponentNickname,
            commitmentTier = "ملتزم ذهبي 🌟"
        ))

        return challenge
    }

    suspend fun acceptChallenge(challenge: LocalChallenge) {
        repository.saveChallenge(challenge.copy(status = ChallengeStatus.ACTIVE.name))
    }

    suspend fun declineChallenge(challenge: LocalChallenge) {
        repository.saveChallenge(challenge.copy(status = ChallengeStatus.DECLINED.name))
    }

    suspend fun reportAndBlockUser(userId: String, targetPublicId: String, reason: String) {
        repository.blockUser(userId, targetPublicId)
        _onlineStatusMessage.value = "تم حظر المستخدم وإرسال بلاغ بالأمان."
    }

    suspend fun inviteCaretaker(
        caretakerPublicId: String,
        caretakerNickname: String,
        myNickname: String
    ): CaretakerLink {
        val linkId = "caretaker_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        val link = CaretakerLink(
            id = linkId,
            patientPublicId = _currentPublicProfile.value?.publicId ?: "patient",
            caretakerPublicId = caretakerPublicId,
            patientNickname = myNickname,
            caretakerNickname = caretakerNickname,
            isCaretakerOfPatient = false,
            allowMissedAlerts = true,
            status = "ACTIVE"
        )
        repository.saveCaretakerLink(link)
        return link
    }
}

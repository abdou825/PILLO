package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.model.Appointment
import com.example.data.model.Badge
import com.example.data.model.BadgeTier
import com.example.data.model.CaretakerLink
import com.example.data.model.Certificate
import com.example.data.model.CoinTransaction
import com.example.data.model.CoinWallet
import com.example.data.model.DailyChallengeProgress
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.FriendRecord
import com.example.data.model.GameScore
import com.example.data.model.HealthReading
import com.example.data.model.LocalChallenge
import com.example.data.model.Medicine
import com.example.data.model.MedicineStatus
import com.example.data.model.MilestoneType
import com.example.data.model.PetState
import com.example.data.model.PublicProfile
import com.example.data.model.StreakInfo
import com.example.data.model.UserProfile
import com.example.data.repository.OnlineSyncService
import com.example.data.repository.PilloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

data class TodayDoseItem(
    val medicine: Medicine,
    val timeLabel: String,
    val scheduledTimeMs: Long,
    val status: DoseStatus,
    val isNext: Boolean = false
)

class PilloViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = PilloRepository(database)
    val syncService = OnlineSyncService(application, repository)

    // Current selected profile ID (defaults to 1 or owner profile)
    private val _currentProfileId = MutableStateFlow(1L)
    val currentProfileId: StateFlow<Long> = _currentProfileId.asStateFlow()

    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = _currentProfileId.flatMapLatest { id ->
        repository.getProfileById(id).combine(MutableStateFlow(Unit)) { prof, _ ->
            prof ?: UserProfile()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val activeMedicines: StateFlow<List<Medicine>> = _currentProfileId.flatMapLatest { id ->
        repository.getActiveMedicines(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val finishedMedicines: StateFlow<List<Medicine>> = _currentProfileId.flatMapLatest { id ->
        repository.getFinishedMedicines(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedicinesGlobal: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Phase 3 StateFlows
    val petState: StateFlow<PetState?> = _currentProfileId.flatMapLatest { id ->
        repository.getPetState(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val coinWallet: StateFlow<CoinWallet?> = _currentProfileId.flatMapLatest { id ->
        repository.getWallet(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedItems: StateFlow<List<String>> = _currentProfileId.flatMapLatest { id ->
        repository.getUnlockedItemIds(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameScores: StateFlow<List<GameScore>> = _currentProfileId.flatMapLatest { id ->
        repository.getGameScores(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val dailyChallenge: StateFlow<DailyChallengeProgress?> = _currentProfileId.flatMapLatest { id ->
        repository.getDailyChallenge(id, todayDateStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val streakInfo: StateFlow<StreakInfo?> = _currentProfileId.flatMapLatest { id ->
        repository.getStreakInfo(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val badges: StateFlow<List<Badge>> = _currentProfileId.flatMapLatest { id ->
        repository.getBadges(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges: StateFlow<List<LocalChallenge>> = _currentProfileId.flatMapLatest { id ->
        repository.getChallenges(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicProfile: StateFlow<PublicProfile?> = syncService.currentPublicProfile
    val isOnline: StateFlow<Boolean> = syncService.isOnline

    val friends: StateFlow<List<FriendRecord>> = repository.getFriends("me")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caretakerLinks: StateFlow<List<CaretakerLink>> = repository.getCaretakerLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's dose logs bounds
    private val todayStartMs: Long
    private val todayEndMs: Long

    init {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        todayStartMs = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        todayEndMs = cal.timeInMillis
    }

    val todayLogs: StateFlow<List<DoseLog>> = repository.getLogsForRange(todayStartMs, todayEndMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Doses for the currently active profile
    val todayDoses: StateFlow<List<TodayDoseItem>> = combine(
        activeMedicines,
        todayLogs
    ) { meds, logs ->
        computeTodayDoseItems(meds, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Doses across ALL family profiles for Family Dashboard
    val allTodayDoses: StateFlow<List<TodayDoseItem>> = combine(
        repository.activeMedicines,
        todayLogs
    ) { meds, logs ->
        computeTodayDoseItems(meds, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun computeTodayDoseItems(meds: List<Medicine>, logs: List<DoseLog>): List<TodayDoseItem> {
        val now = System.currentTimeMillis()
        val items = mutableListOf<TodayDoseItem>()
        val logMap = logs.associateBy { "${it.medicineId}_${it.timeLabel}" }
        val cal = Calendar.getInstance()

        for (med in meds) {
            val times = med.customScheduleTimes.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            for (timeStr in times) {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: continue
                    val min = parts[1].toIntOrNull() ?: continue
                    val doseCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, min)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val scheduledMs = doseCal.timeInMillis
                    val key = "${med.id}_$timeStr"
                    val log = logMap[key]

                    val status = log?.status ?: DoseStatus.PENDING

                    items.add(
                        TodayDoseItem(
                            medicine = med,
                            timeLabel = timeStr,
                            scheduledTimeMs = scheduledMs,
                            status = status,
                            isNext = false
                        )
                    )
                }
            }
        }

        val sorted = items.sortedBy { it.scheduledTimeMs }
        var nextFound = false
        return sorted.map { item ->
            if (!nextFound && item.status == DoseStatus.PENDING && item.scheduledTimeMs >= now - (15 * 60 * 1000L)) {
                nextFound = true
                item.copy(isNext = true)
            } else {
                item
            }
        }
    }

    val lowStockMedicines: StateFlow<List<Medicine>> = activeMedicines.combine(MutableStateFlow(Unit)) { meds, _ ->
        meds.filter { med ->
            val timesPerDay = med.timesPerDay.coerceAtLeast(1)
            val daysLeft = ceil(med.remainingQuantity.toDouble() / timesPerDay.toDouble()).toInt()
            daysLeft in 1..2
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Certificates for active profile
    val certificates: StateFlow<List<Certificate>> = _currentProfileId.flatMapLatest { id ->
        repository.getCertificates(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Health readings for active profile
    val healthReadings: StateFlow<List<HealthReading>> = _currentProfileId.flatMapLatest { id ->
        repository.getAllReadings(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Appointments for active profile
    val appointments: StateFlow<List<Appointment>> = _currentProfileId.flatMapLatest { id ->
        repository.getUpcomingAppointments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchProfile(profileId: Long) {
        _currentProfileId.value = profileId
    }

    fun addProfile(profile: UserProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.saveUserProfile(profile)
            _currentProfileId.value = newId
            AlarmScheduler.rescheduleAll(getApplication())
            onComplete()
        }
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            _currentProfileId.value = 1L
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun saveUserProfile(profile: UserProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            AlarmScheduler.rescheduleAll(getApplication())
            onComplete()
        }
    }

    fun saveMedicine(medicine: Medicine, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val medToSave = medicine.copy(profileId = _currentProfileId.value)
            if (medToSave.id == 0L) {
                repository.insertMedicine(medToSave)
            } else {
                repository.updateMedicine(medToSave)
            }
            AlarmScheduler.rescheduleAll(getApplication())
            onComplete()
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun markDoseTaken(medicineId: Long, scheduledTimeMs: Long, timeLabel: String) {
        viewModelScope.launch {
            repository.markDoseTaken(medicineId, scheduledTimeMs, timeLabel)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun markDoseSkipped(medicineId: Long, scheduledTimeMs: Long, timeLabel: String) {
        viewModelScope.launch {
            repository.markDoseSkipped(medicineId, scheduledTimeMs, timeLabel)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun replenishMedicine(medicineId: Long, additionalQuantity: Int) {
        viewModelScope.launch {
            repository.replenishMedicine(medicineId, additionalQuantity)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun finishMedicine(medicineId: Long) {
        viewModelScope.launch {
            repository.finishMedicine(medicineId)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun completeTreatment(medicine: Medicine) {
        viewModelScope.launch {
            repository.finishMedicine(medicine.id)
            repository.issueCompletionCertificate(medicine, 96)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun extendTreatment(medicine: Medicine, extraQuantity: Int) {
        viewModelScope.launch {
            repository.issueExtensionCertificate(medicine, 94)
            val updated = medicine.copy(
                remainingQuantity = extraQuantity,
                totalQuantity = maxOf(medicine.totalQuantity, extraQuantity),
                treatmentPeriodNumber = medicine.treatmentPeriodNumber + 1,
                periodStartDate = System.currentTimeMillis(),
                status = MedicineStatus.ACTIVE,
                isActive = true
            )
            repository.updateMedicine(updated)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun generateSampleCertificate() {
        viewModelScope.launch {
            val cert = Certificate(
                profileId = _currentProfileId.value,
                medicineName = "بانادول إكسترا",
                milestoneType = MilestoneType.DAY_30,
                badgeTier = BadgeTier.SILVER,
                daysCount = 30,
                adherencePercent = 98,
                startDate = System.currentTimeMillis() - (30L * 24 * 3600 * 1000),
                endDate = System.currentTimeMillis(),
                certificateNumber = certificates.value.size + 1,
                hideMedicineName = false
            )
            repository.insertCertificate(cert)
        }
    }

    fun deleteCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.deleteCertificate(certificate)
        }
    }

    fun addHealthReading(reading: HealthReading) {
        viewModelScope.launch {
            repository.insertHealthReading(reading.copy(profileId = _currentProfileId.value))
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val id = repository.insertAppointment(appointment.copy(profileId = _currentProfileId.value))
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    // --- Phase 3: Pet Methods ---
    fun feedPet() {
        viewModelScope.launch {
            val pet = repository.getPetStateSync(_currentProfileId.value) ?: PetState(profileId = _currentProfileId.value)
            val newHappiness = (pet.happiness + 20).coerceAtMost(150)
            val newStage = when {
                newHappiness >= 120 -> 4
                newHappiness >= 70 -> 3
                newHappiness >= 30 -> 2
                else -> 1
            }
            repository.savePetState(pet.copy(happiness = newHappiness, stage = maxOf(pet.stage, newStage), lastFedAt = System.currentTimeMillis()))
        }
    }

    fun changePetSpecies(species: String) {
        viewModelScope.launch {
            val pet = repository.getPetStateSync(_currentProfileId.value) ?: PetState(profileId = _currentProfileId.value)
            repository.savePetState(pet.copy(species = species))
        }
    }

    fun equipPetItem(itemId: String) {
        viewModelScope.launch {
            val pet = repository.getPetStateSync(_currentProfileId.value) ?: PetState(profileId = _currentProfileId.value)
            repository.savePetState(pet.copy(equippedItem = itemId))
        }
    }

    fun unlockStoreItem(itemId: String, cost: Int, name: String) {
        viewModelScope.launch {
            repository.unlockStoreItem(_currentProfileId.value, itemId, cost, "شراء $name للأليف 🎁")
        }
    }

    // --- Phase 3: Games Methods ---
    fun recordGameScore(gameId: String, score: Int) {
        viewModelScope.launch {
            repository.recordGameScore(_currentProfileId.value, gameId, score)
        }
    }

    fun completeDailyChallenge() {
        viewModelScope.launch {
            repository.completeDailyChallenge(_currentProfileId.value, todayDateStr)
        }
    }

    // --- Phase 3: Social & Account Methods ---
    fun createOrUpdateAccount(nickname: String, avatarId: String, isMinor: Boolean, parentConsent: Boolean) {
        syncService.createOrUpdateAccount(nickname, avatarId, isMinor, parentConsent)
    }

    fun signInWithGoogle(
        name: String,
        email: String,
        photoUrl: String = "",
        avatarId: String = "avatar_1",
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                displayName = name.ifEmpty { current.displayName },
                userEmail = email.ifEmpty { current.userEmail },
                googlePhotoUrl = photoUrl,
                avatarId = avatarId,
                isGoogleLinked = true
            )
            repository.saveUserProfile(updated)
            syncService.createOrUpdateAccount(
                nickname = updated.displayName,
                avatarId = updated.avatarId,
                isMinor = updated.ageMode == com.example.data.model.AgeMode.KIDS || updated.ageMode == com.example.data.model.AgeMode.TEENS,
                parentConsent = true
            )
            onSuccess?.invoke()
        }
    }

    fun updateUserAccountDetails(name: String, email: String, fatherPhone: String, avatarId: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                displayName = name,
                userEmail = email,
                fatherPhone = fatherPhone,
                emergencyContactPhone = if (current.emergencyContactPhone.isEmpty()) fatherPhone else current.emergencyContactPhone,
                avatarId = avatarId
            )
            repository.saveUserProfile(updated)
            syncService.createOrUpdateAccount(name, avatarId, current.ageMode == com.example.data.model.AgeMode.KIDS || current.ageMode == com.example.data.model.AgeMode.TEENS, true)
        }
    }

    fun rewardSonWithCoins(sonProfileId: Long = 1L, amount: Int = 50, reason: String = "مكافأة تشجيعية من ولي الأمر 🌟") {
        viewModelScope.launch {
            val wallet = repository.getWalletSync(sonProfileId) ?: com.example.data.model.CoinWallet(profileId = sonProfileId, balance = 0)
            repository.saveWallet(wallet.copy(balance = wallet.balance + amount))
        }
    }

    fun deleteAccount() {
        syncService.deleteAccountAndOnlineData()
    }

    fun regenerateQr(): String {
        return syncService.regenerateQrCode()
    }

    fun sendChallenge(opponentPublicId: String, opponentNickname: String, type: String, durationDays: Int) {
        viewModelScope.launch {
            syncService.sendChallengeRequest(_currentProfileId.value, opponentPublicId, opponentNickname, type, durationDays)
        }
    }

    fun acceptChallenge(challenge: LocalChallenge) {
        viewModelScope.launch {
            syncService.acceptChallenge(challenge)
        }
    }

    fun declineChallenge(challenge: LocalChallenge) {
        viewModelScope.launch {
            syncService.declineChallenge(challenge)
        }
    }

    fun blockUser(targetId: String) {
        viewModelScope.launch {
            syncService.reportAndBlockUser("me", targetId, "حظر بناء على طلب المستخدم")
        }
    }

    fun inviteCaretaker(caretakerId: String, caretakerName: String) {
        viewModelScope.launch {
            val myName = userProfile.value.displayName
            syncService.inviteCaretaker(caretakerId, caretakerName, myName)
        }
    }

    fun testAlarm(delaySeconds: Int = 10) {
        AlarmScheduler.scheduleTestAlarm(getApplication(), delaySeconds)
    }
}

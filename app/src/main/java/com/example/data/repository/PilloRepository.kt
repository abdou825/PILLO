package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Appointment
import com.example.data.model.Badge
import com.example.data.model.BadgeTier
import com.example.data.model.BlockedUser
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
import com.example.data.model.HealthReadingType
import com.example.data.model.LocalChallenge
import com.example.data.model.Medicine
import com.example.data.model.MedicineStatus
import com.example.data.model.MilestoneType
import com.example.data.model.PetState
import com.example.data.model.StreakInfo
import com.example.data.model.UnlockedItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class PilloRepository(private val database: AppDatabase) {
    private val medicineDao = database.medicineDao()
    private val userProfileDao = database.userProfileDao()
    private val doseLogDao = database.doseLogDao()
    private val certificateDao = database.certificateDao()
    private val healthReadingDao = database.healthReadingDao()
    private val appointmentDao = database.appointmentDao()

    private val petDao = database.petDao()
    private val coinDao = database.coinDao()
    private val gameDao = database.gameDao()
    private val socialDao = database.socialDao()

    val allProfiles: Flow<List<UserProfile>> = userProfileDao.getAllProfiles()
    val activeMedicines: Flow<List<Medicine>> = medicineDao.getActiveMedicines()
    val finishedMedicines: Flow<List<Medicine>> = medicineDao.getFinishedMedicines()
    val allMedicines: Flow<List<Medicine>> = medicineDao.getAllMedicines()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    fun getActiveMedicines(profileId: Long): Flow<List<Medicine>> = medicineDao.getActiveMedicines(profileId)
    fun getFinishedMedicines(profileId: Long): Flow<List<Medicine>> = medicineDao.getFinishedMedicines(profileId)
    fun getProfileById(id: Long): Flow<UserProfile?> = userProfileDao.getProfileById(id)
    suspend fun getProfileByIdSync(id: Long): UserProfile? = userProfileDao.getProfileByIdSync(id)

    suspend fun getActiveMedicinesSync(): List<Medicine> = medicineDao.getActiveMedicinesSync()
    suspend fun getUserProfileSync(): UserProfile? = userProfileDao.getUserProfileSync()
    suspend fun getMedicineByIdSync(id: Long): Medicine? = medicineDao.getMedicineByIdSync(id)
    fun getMedicineById(id: Long): Flow<Medicine?> = medicineDao.getMedicineById(id)

    suspend fun insertMedicine(medicine: Medicine): Long = medicineDao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: Medicine) = medicineDao.updateMedicine(medicine)
    suspend fun deleteMedicine(medicine: Medicine) {
        doseLogDao.deleteLogsForMedicine(medicine.id)
        medicineDao.deleteMedicine(medicine)
    }

    suspend fun saveUserProfile(profile: UserProfile): Long = userProfileDao.insertOrUpdate(profile)
    suspend fun deleteProfile(profile: UserProfile) = userProfileDao.deleteProfile(profile)

    fun getLogsForRange(startTime: Long, endTime: Long): Flow<List<DoseLog>> =
        doseLogDao.getLogsForRange(startTime, endTime)

    fun getLogsForProfileAndRange(profileId: Long, startTime: Long, endTime: Long): Flow<List<DoseLog>> =
        doseLogDao.getLogsForProfileAndRange(profileId, startTime, endTime)

    fun getLogsForMedicine(medicineId: Long): Flow<List<DoseLog>> =
        doseLogDao.getLogsForMedicine(medicineId)

    suspend fun recordOrUpdateDose(
        profileId: Long,
        medicineId: Long,
        scheduledTime: Long,
        timeLabel: String,
        status: DoseStatus
    ): DoseLog {
        val existing = doseLogDao.getLogByMedicineAndTime(medicineId, scheduledTime)
        val now = System.currentTimeMillis()
        val logToSave = if (existing != null) {
            existing.copy(status = status, actionTime = now)
        } else {
            DoseLog(
                profileId = profileId,
                medicineId = medicineId,
                scheduledTime = scheduledTime,
                timeLabel = timeLabel,
                status = status,
                actionTime = now
            )
        }
        val id = doseLogDao.insertLog(logToSave)
        return logToSave.copy(id = if (existing != null) existing.id else id)
    }

    suspend fun markDoseTaken(medicineId: Long, scheduledTime: Long, timeLabel: String) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        val profileId = medicine?.profileId ?: 1L
        recordOrUpdateDose(profileId, medicineId, scheduledTime, timeLabel, DoseStatus.TAKEN)

        if (medicine != null && medicine.remainingQuantity > 0) {
            val newRemaining = medicine.remainingQuantity - 1
            val isFinished = newRemaining <= 0
            val updated = medicine.copy(
                remainingQuantity = newRemaining,
                status = if (isFinished) MedicineStatus.FINISHED else MedicineStatus.ACTIVE,
                isActive = !isFinished
            )
            medicineDao.updateMedicine(updated)

            // Check milestone certificates
            checkAndGenerateMilestones(updated)
        }

        // Phase 3: Reward coins, feed pet, advance streak
        rewardDoseTaken(profileId)
    }

    private suspend fun rewardDoseTaken(profileId: Long) {
        // 1. Add coins
        val wallet = coinDao.getWalletSync(profileId) ?: CoinWallet(profileId = profileId, balance = 20)
        val newBalance = wallet.balance + 10
        coinDao.saveWallet(wallet.copy(balance = newBalance))
        coinDao.insertTransaction(CoinTransaction(
            profileId = profileId,
            amount = 10,
            reason = "أخذ الجرعة في موعدها المحدد 💊"
        ))

        // 2. Feed and cheer up pet
        val pet = petDao.getPetStateSync(profileId) ?: PetState(profileId = profileId)
        val newHappiness = (pet.happiness + 25).coerceAtMost(150)
        val newStage = when {
            newHappiness >= 120 -> 4
            newHappiness >= 70 -> 3
            newHappiness >= 30 -> 2
            else -> 1
        }
        petDao.savePetState(pet.copy(
            happiness = newHappiness,
            stage = maxOf(pet.stage, newStage),
            lastFedAt = System.currentTimeMillis()
        ))

        // 3. Update Streak & Badges
        val streak = gameDao.getStreakInfoSync(profileId) ?: StreakInfo(profileId = profileId)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (streak.lastDoseDay != todayStr) {
            val nextStreak = streak.currentStreak + 1
            val best = maxOf(streak.bestStreak, nextStreak)
            gameDao.saveStreakInfo(streak.copy(
                currentStreak = nextStreak,
                bestStreak = best,
                lastDoseDay = todayStr
            ))

            // Bonus reward on streak milestones (3, 7, 14, 30 days)
            if (nextStreak in listOf(3, 7, 14, 30)) {
                val bonus = 20
                val w = coinDao.getWalletSync(profileId) ?: CoinWallet(profileId = profileId, balance = 0)
                coinDao.saveWallet(w.copy(balance = w.balance + bonus))
                coinDao.insertTransaction(CoinTransaction(
                    profileId = profileId,
                    amount = bonus,
                    reason = "مكافأة سلسلة التزام متواصل $nextStreak أيام! 🔥"
                ))
                gameDao.insertBadge(Badge(
                    profileId = profileId,
                    badgeId = "streak_$nextStreak",
                    titleArabic = "بطل الالتزام $nextStreak أيام",
                    descriptionArabic = "حافظت على مواعيدك بانتظام ودون انقطاع!",
                    iconName = "flame"
                ))
            }
        }
    }

    suspend fun markDoseSkipped(medicineId: Long, scheduledTime: Long, timeLabel: String) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        val profileId = medicine?.profileId ?: 1L
        recordOrUpdateDose(profileId, medicineId, scheduledTime, timeLabel, DoseStatus.SKIPPED)
    }

    suspend fun markDoseSnoozed(medicineId: Long, scheduledTime: Long, timeLabel: String) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        val profileId = medicine?.profileId ?: 1L
        recordOrUpdateDose(profileId, medicineId, scheduledTime, timeLabel, DoseStatus.SNOOZED)
    }

    suspend fun markDoseMissed(medicineId: Long, scheduledTime: Long, timeLabel: String) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        val profileId = medicine?.profileId ?: 1L
        recordOrUpdateDose(profileId, medicineId, scheduledTime, timeLabel, DoseStatus.MISSED)

        // Never punish with coin loss or guilt. Pet gently rests/looks slightly tired.
        val pet = petDao.getPetStateSync(profileId) ?: PetState(profileId = profileId)
        val reducedHappiness = (pet.happiness - 5).coerceAtLeast(10)
        petDao.savePetState(pet.copy(happiness = reducedHappiness))
    }

    suspend fun replenishMedicine(medicineId: Long, additionalQuantity: Int) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        if (medicine != null) {
            val newRemaining = medicine.remainingQuantity + additionalQuantity
            val newTotal = maxOf(medicine.totalQuantity, newRemaining)
            val updated = medicine.copy(
                totalQuantity = newTotal,
                remainingQuantity = newRemaining,
                status = MedicineStatus.ACTIVE,
                isActive = newRemaining > 0
            )
            medicineDao.updateMedicine(updated)
        }
    }

    suspend fun finishMedicine(medicineId: Long) {
        val medicine = medicineDao.getMedicineByIdSync(medicineId)
        if (medicine != null) {
            val updated = medicine.copy(
                remainingQuantity = 0,
                status = MedicineStatus.FINISHED,
                isActive = false
            )
            medicineDao.updateMedicine(updated)
        }
    }

    // --- Certificates ---
    fun getCertificates(profileId: Long): Flow<List<Certificate>> =
        certificateDao.getCertificatesForProfile(profileId)

    fun getAllCertificates(): Flow<List<Certificate>> =
        certificateDao.getAllCertificates()

    suspend fun insertCertificate(cert: Certificate): Long =
        certificateDao.insertCertificate(cert)

    suspend fun deleteCertificate(cert: Certificate) =
        certificateDao.deleteCertificate(cert)

    suspend fun checkAndGenerateMilestones(medicine: Medicine) {
        val daysElapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - medicine.startDate).toInt()
        val milestones = listOf(7, 14, 30, 90, 180, 365)
        for (m in milestones) {
            if (daysElapsed == m) {
                val tier = when {
                    m >= 365 -> BadgeTier.DIAMOND
                    m >= 90 -> BadgeTier.GOLD
                    m >= 30 -> BadgeTier.SILVER
                    else -> BadgeTier.BRONZE
                }
                val mType = when (m) {
                    7 -> MilestoneType.DAY_7
                    14 -> MilestoneType.DAY_14
                    30 -> MilestoneType.DAY_30
                    90 -> MilestoneType.DAY_90
                    180 -> MilestoneType.DAY_180
                    else -> MilestoneType.DAY_365
                }
                val count = certificateDao.getCertificateCount(medicine.profileId) + 1
                val cert = Certificate(
                    profileId = medicine.profileId,
                    medicineName = medicine.name,
                    milestoneType = mType,
                    badgeTier = tier,
                    daysCount = m,
                    adherencePercent = 95, // High adherence milestone
                    startDate = medicine.startDate,
                    endDate = System.currentTimeMillis(),
                    certificateNumber = count
                )
                certificateDao.insertCertificate(cert)
            }
        }
    }

    suspend fun issueCompletionCertificate(medicine: Medicine, adherencePercent: Int): Certificate {
        val count = certificateDao.getCertificateCount(medicine.profileId) + 1
        val days = maxOf(1, TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - medicine.startDate).toInt())
        val tier = if (days >= 90) BadgeTier.DIAMOND else if (days >= 30) BadgeTier.GOLD else BadgeTier.SILVER
        val cert = Certificate(
            profileId = medicine.profileId,
            medicineName = medicine.name,
            milestoneType = MilestoneType.COMPLETION,
            badgeTier = tier,
            daysCount = days,
            adherencePercent = adherencePercent,
            startDate = medicine.startDate,
            endDate = System.currentTimeMillis(),
            certificateNumber = count
        )
        certificateDao.insertCertificate(cert)
        return cert
    }

    suspend fun issueExtensionCertificate(medicine: Medicine, adherencePercent: Int): Certificate {
        val count = certificateDao.getCertificateCount(medicine.profileId) + 1
        val days = maxOf(1, TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - medicine.periodStartDate).toInt())
        val cert = Certificate(
            profileId = medicine.profileId,
            medicineName = medicine.name,
            milestoneType = MilestoneType.EXTENSION_PERIOD,
            badgeTier = BadgeTier.SILVER,
            daysCount = days,
            adherencePercent = adherencePercent,
            startDate = medicine.periodStartDate,
            endDate = System.currentTimeMillis(),
            certificateNumber = count
        )
        certificateDao.insertCertificate(cert)
        return cert
    }

    // --- Health Readings ---
    fun getReadings(profileId: Long, type: HealthReadingType): Flow<List<HealthReading>> =
        healthReadingDao.getReadingsByType(profileId, type)

    fun getAllReadings(profileId: Long): Flow<List<HealthReading>> =
        healthReadingDao.getAllReadingsForProfile(profileId)

    suspend fun insertHealthReading(reading: HealthReading): Long =
        healthReadingDao.insertReading(reading)

    suspend fun deleteHealthReading(reading: HealthReading) =
        healthReadingDao.deleteReading(reading)

    suspend fun getReadingsSince(profileId: Long, sinceMs: Long): List<HealthReading> =
        healthReadingDao.getReadingsSince(profileId, sinceMs)

    // --- Appointments ---
    fun getUpcomingAppointments(profileId: Long, now: Long = System.currentTimeMillis()): Flow<List<Appointment>> =
        appointmentDao.getUpcomingAppointments(profileId, now)

    fun getAllAppointments(profileId: Long): Flow<List<Appointment>> =
        appointmentDao.getAllAppointments(profileId)

    suspend fun insertAppointment(appointment: Appointment): Long =
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: Appointment) =
        appointmentDao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: Appointment) =
        appointmentDao.deleteAppointment(appointment)

    // --- Phase 3: Pet ---
    fun getPetState(profileId: Long): Flow<PetState?> = petDao.getPetState(profileId)
    suspend fun getPetStateSync(profileId: Long): PetState? = petDao.getPetStateSync(profileId)
    suspend fun savePetState(petState: PetState) = petDao.savePetState(petState)

    // --- Phase 3: Coins & Store ---
    fun getWallet(profileId: Long): Flow<CoinWallet?> = coinDao.getWallet(profileId)
    suspend fun getWalletSync(profileId: Long): CoinWallet? = coinDao.getWalletSync(profileId)
    suspend fun saveWallet(wallet: CoinWallet) = coinDao.saveWallet(wallet)
    fun getRecentTransactions(profileId: Long): Flow<List<CoinTransaction>> = coinDao.getRecentTransactions(profileId)
    fun getUnlockedItemIds(profileId: Long): Flow<List<String>> = coinDao.getUnlockedItemIds(profileId)

    suspend fun spendCoins(profileId: Long, amount: Int, reason: String): Boolean {
        val current = coinDao.getWalletSync(profileId) ?: CoinWallet(profileId = profileId, balance = 20)
        if (current.balance >= amount) {
            val newBalance = current.balance - amount
            coinDao.saveWallet(current.copy(balance = newBalance))
            coinDao.insertTransaction(CoinTransaction(
                profileId = profileId,
                amount = -amount,
                reason = reason
            ))
            return true
        }
        return false
    }

    suspend fun unlockStoreItem(profileId: Long, itemId: String, cost: Int, reason: String): Boolean {
        val success = spendCoins(profileId, cost, reason)
        if (success) {
            coinDao.unlockItem(UnlockedItem(profileId, itemId))
        }
        return success
    }

    // --- Phase 3: Mini-Games & Daily Challenges ---
    fun getGameScores(profileId: Long): Flow<List<GameScore>> = gameDao.getAllScores(profileId)

    suspend fun recordGameScore(profileId: Long, gameId: String, newScore: Int) {
        val existing = gameDao.getScoreForGame(profileId, gameId)
        val best = if (existing != null) maxOf(existing.bestScore, newScore) else newScore
        gameDao.saveScore(GameScore(
            id = existing?.id ?: 0,
            profileId = profileId,
            gameId = gameId,
            bestScore = best,
            lastPlayedAt = System.currentTimeMillis()
        ))

        // Award play coins (3 coins per completed game)
        val wallet = coinDao.getWalletSync(profileId) ?: CoinWallet(profileId = profileId, balance = 20)
        coinDao.saveWallet(wallet.copy(balance = wallet.balance + 3))
        coinDao.insertTransaction(CoinTransaction(
            profileId = profileId,
            amount = 3,
            reason = "مكافأة تجربة لعبة في وقت الفراغ 🎮"
        ))
    }

    fun getDailyChallenge(profileId: Long, dateStr: String): Flow<DailyChallengeProgress?> =
        gameDao.getDailyChallenge(profileId, dateStr)

    suspend fun saveDailyChallenge(challenge: DailyChallengeProgress) =
        gameDao.saveDailyChallenge(challenge)

    suspend fun completeDailyChallenge(profileId: Long, dateStr: String) {
        val challenge = DailyChallengeProgress(
            profileId = profileId,
            challengeDate = dateStr,
            taskType = "TAKE_ALL_DOSES",
            titleArabic = "التزم بجميع جرعاتك اليوم 🌟",
            isCompleted = true,
            coinsReward = 30
        )
        gameDao.saveDailyChallenge(challenge)

        val wallet = coinDao.getWalletSync(profileId) ?: CoinWallet(profileId = profileId, balance = 20)
        coinDao.saveWallet(wallet.copy(balance = wallet.balance + 30))
        coinDao.insertTransaction(CoinTransaction(
            profileId = profileId,
            amount = 30,
            reason = "إنجاز التحدي اليومي لالتزام الدواء 🏆"
        ))
    }

    fun getStreakInfo(profileId: Long): Flow<StreakInfo?> = gameDao.getStreakInfo(profileId)
    fun getBadges(profileId: Long): Flow<List<Badge>> = gameDao.getBadges(profileId)

    // --- Phase 3: Challenges & Friends ---
    fun getChallenges(profileId: Long): Flow<List<LocalChallenge>> = socialDao.getChallenges(profileId)
    suspend fun saveChallenge(challenge: LocalChallenge) = socialDao.saveChallenge(challenge)
    suspend fun deleteChallenge(challenge: LocalChallenge) = socialDao.deleteChallenge(challenge)

    fun getFriends(userId: String): Flow<List<FriendRecord>> = socialDao.getFriends(userId)
    suspend fun saveFriend(friend: FriendRecord) = socialDao.saveFriend(friend)
    suspend fun removeFriend(friend: FriendRecord) = socialDao.removeFriend(friend)

    fun getBlockedUsers(userId: String): Flow<List<BlockedUser>> = socialDao.getBlockedUsers(userId)
    suspend fun blockUser(userId: String, targetPublicId: String) {
        socialDao.blockUser(BlockedUser(userId, targetPublicId))
    }
    suspend fun unblockUser(userId: String, targetPublicId: String) {
        socialDao.unblockUser(BlockedUser(userId, targetPublicId))
    }

    // --- Phase 3: Caretaker Notifications ---
    fun getCaretakerLinks(): Flow<List<CaretakerLink>> = socialDao.getCaretakerLinks()
    suspend fun saveCaretakerLink(link: CaretakerLink) = socialDao.saveCaretakerLink(link)
    suspend fun deleteCaretakerLink(link: CaretakerLink) = socialDao.deleteCaretakerLink(link)
}

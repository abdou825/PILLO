package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Appointment
import com.example.data.model.Badge
import com.example.data.model.BlockedUser
import com.example.data.model.CaretakerLink
import com.example.data.model.Certificate
import com.example.data.model.CoinTransaction
import com.example.data.model.CoinWallet
import com.example.data.model.DailyChallengeProgress
import com.example.data.model.DoseLog
import com.example.data.model.FriendRecord
import com.example.data.model.GameScore
import com.example.data.model.HealthReading
import com.example.data.model.LocalChallenge
import com.example.data.model.Medicine
import com.example.data.model.PetState
import com.example.data.model.StreakInfo
import com.example.data.model.UnlockedItem
import com.example.data.model.UserProfile

@Database(
    entities = [
        Medicine::class,
        UserProfile::class,
        DoseLog::class,
        Certificate::class,
        HealthReading::class,
        Appointment::class,
        PetState::class,
        CoinWallet::class,
        CoinTransaction::class,
        UnlockedItem::class,
        GameScore::class,
        DailyChallengeProgress::class,
        StreakInfo::class,
        Badge::class,
        LocalChallenge::class,
        FriendRecord::class,
        BlockedUser::class,
        CaretakerLink::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun certificateDao(): CertificateDao
    abstract fun healthReadingDao(): HealthReadingDao
    abstract fun appointmentDao(): AppointmentDao

    abstract fun petDao(): PetDao
    abstract fun coinDao(): CoinDao
    abstract fun gameDao(): GameDao
    abstract fun socialDao(): SocialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `certificates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `medicineName` TEXT NOT NULL,
                        `milestoneType` TEXT NOT NULL,
                        `badgeTier` TEXT NOT NULL,
                        `daysCount` INTEGER NOT NULL,
                        `adherencePercent` INTEGER NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `certificateNumber` INTEGER NOT NULL,
                        `hideMedicineName` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `health_readings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `value1` REAL NOT NULL,
                        `value2` REAL,
                        `timestamp` INTEGER NOT NULL,
                        `note` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `appointments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `dateTimeMs` INTEGER NOT NULL,
                        `location` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pet_state` (
                        `profileId` INTEGER NOT NULL,
                        `species` TEXT NOT NULL,
                        `stage` INTEGER NOT NULL,
                        `happiness` INTEGER NOT NULL,
                        `lastFedAt` INTEGER NOT NULL,
                        `equippedItem` TEXT NOT NULL,
                        PRIMARY KEY(`profileId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `coin_wallet` (
                        `profileId` INTEGER NOT NULL,
                        `balance` INTEGER NOT NULL,
                        PRIMARY KEY(`profileId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `coin_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `unlocked_items` (
                        `profileId` INTEGER NOT NULL,
                        `itemId` TEXT NOT NULL,
                        `unlockedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`profileId`, `itemId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `game_scores` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `gameId` TEXT NOT NULL,
                        `bestScore` INTEGER NOT NULL,
                        `lastPlayedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_challenges` (
                        `profileId` INTEGER NOT NULL,
                        `challengeDate` TEXT NOT NULL,
                        `taskType` TEXT NOT NULL,
                        `titleArabic` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `coinsReward` INTEGER NOT NULL,
                        PRIMARY KEY(`profileId`, `challengeDate`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `streak_info` (
                        `profileId` INTEGER NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `bestStreak` INTEGER NOT NULL,
                        `lastDoseDay` TEXT NOT NULL,
                        PRIMARY KEY(`profileId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `badges` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `badgeId` TEXT NOT NULL,
                        `titleArabic` TEXT NOT NULL,
                        `descriptionArabic` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `earnedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `local_challenges` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `profileId` INTEGER NOT NULL,
                        `opponentPublicId` TEXT NOT NULL,
                        `opponentNickname` TEXT NOT NULL,
                        `challengeType` TEXT NOT NULL,
                        `durationDays` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `myCommitmentPercent` INTEGER NOT NULL,
                        `opponentCommitmentPercent` INTEGER NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `friends` (
                        `userId` TEXT NOT NULL,
                        `friendPublicId` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `commitmentTier` TEXT NOT NULL,
                        `streak` INTEGER NOT NULL,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`, `friendPublicId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `blocked_users` (
                        `userId` TEXT NOT NULL,
                        `blockedPublicId` TEXT NOT NULL,
                        `blockedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`, `blockedPublicId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `caretaker_links` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `patientPublicId` TEXT NOT NULL,
                        `caretakerPublicId` TEXT NOT NULL,
                        `patientNickname` TEXT NOT NULL,
                        `caretakerNickname` TEXT NOT NULL,
                        `isCaretakerOfPatient` INTEGER NOT NULL,
                        `allowMissedAlerts` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `userEmail` TEXT NOT NULL DEFAULT 'hero@pillo.app'")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `fatherPhone` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `avatarId` TEXT NOT NULL DEFAULT 'avatar_1'")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `isParentMode` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `linkedSonEmail` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `googlePhotoUrl` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `isGoogleLinked` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pillo_database.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

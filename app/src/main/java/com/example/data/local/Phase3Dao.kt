package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Badge
import com.example.data.model.BlockedUser
import com.example.data.model.CaretakerLink
import com.example.data.model.CoinTransaction
import com.example.data.model.CoinWallet
import com.example.data.model.DailyChallengeProgress
import com.example.data.model.FriendRecord
import com.example.data.model.GameScore
import com.example.data.model.LocalChallenge
import com.example.data.model.PetState
import com.example.data.model.StreakInfo
import com.example.data.model.UnlockedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pet_state WHERE profileId = :profileId LIMIT 1")
    fun getPetState(profileId: Long): Flow<PetState?>

    @Query("SELECT * FROM pet_state WHERE profileId = :profileId LIMIT 1")
    suspend fun getPetStateSync(profileId: Long): PetState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePetState(petState: PetState)
}

@Dao
interface CoinDao {
    @Query("SELECT * FROM coin_wallet WHERE profileId = :profileId LIMIT 1")
    fun getWallet(profileId: Long): Flow<CoinWallet?>

    @Query("SELECT * FROM coin_wallet WHERE profileId = :profileId LIMIT 1")
    suspend fun getWalletSync(profileId: Long): CoinWallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: CoinWallet)

    @Insert
    suspend fun insertTransaction(transaction: CoinTransaction): Long

    @Query("SELECT * FROM coin_transactions WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT 20")
    fun getRecentTransactions(profileId: Long): Flow<List<CoinTransaction>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockItem(item: UnlockedItem)

    @Query("SELECT itemId FROM unlocked_items WHERE profileId = :profileId")
    fun getUnlockedItemIds(profileId: Long): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM unlocked_items WHERE profileId = :profileId AND itemId = :itemId)")
    suspend fun isItemUnlocked(profileId: Long, itemId: String): Boolean
}

@Dao
interface GameDao {
    @Query("SELECT * FROM game_scores WHERE profileId = :profileId AND gameId = :gameId LIMIT 1")
    suspend fun getScoreForGame(profileId: Long, gameId: String): GameScore?

    @Query("SELECT * FROM game_scores WHERE profileId = :profileId ORDER BY bestScore DESC")
    fun getAllScores(profileId: Long): Flow<List<GameScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScore(score: GameScore)

    @Query("SELECT * FROM daily_challenges WHERE profileId = :profileId AND challengeDate = :date LIMIT 1")
    fun getDailyChallenge(profileId: Long, date: String): Flow<DailyChallengeProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyChallenge(challenge: DailyChallengeProgress)

    @Query("SELECT * FROM streak_info WHERE profileId = :profileId LIMIT 1")
    fun getStreakInfo(profileId: Long): Flow<StreakInfo?>

    @Query("SELECT * FROM streak_info WHERE profileId = :profileId LIMIT 1")
    suspend fun getStreakInfoSync(profileId: Long): StreakInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStreakInfo(streakInfo: StreakInfo)

    @Query("SELECT * FROM badges WHERE profileId = :profileId ORDER BY earnedAt DESC")
    fun getBadges(profileId: Long): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: Badge): Long
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM local_challenges WHERE profileId = :profileId ORDER BY startDate DESC")
    fun getChallenges(profileId: Long): Flow<List<LocalChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChallenge(challenge: LocalChallenge)

    @Delete
    suspend fun deleteChallenge(challenge: LocalChallenge)

    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY streak DESC")
    fun getFriends(userId: String): Flow<List<FriendRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFriend(friend: FriendRecord)

    @Delete
    suspend fun removeFriend(friend: FriendRecord)

    @Query("SELECT * FROM blocked_users WHERE userId = :userId")
    fun getBlockedUsers(userId: String): Flow<List<BlockedUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockUser(blockedUser: BlockedUser)

    @Delete
    suspend fun unblockUser(blockedUser: BlockedUser)

    @Query("SELECT * FROM caretaker_links ORDER BY updatedAt DESC")
    fun getCaretakerLinks(): Flow<List<CaretakerLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCaretakerLink(link: CaretakerLink)

    @Delete
    suspend fun deleteCaretakerLink(link: CaretakerLink)
}

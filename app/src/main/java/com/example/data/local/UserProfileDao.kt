package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile ORDER BY isOwner DESC, id ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdSync(id: Long): UserProfile?

    @Query("SELECT * FROM user_profile WHERE isOwner = 1 LIMIT 1")
    fun getOwnerProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE isOwner = 1 LIMIT 1")
    suspend fun getOwnerProfileSync(): UserProfile?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile): Long

    @Update
    suspend fun update(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
}

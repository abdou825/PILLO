package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HealthReading
import com.example.data.model.HealthReadingType
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthReadingDao {
    @Query("SELECT * FROM health_readings WHERE profileId = :profileId AND type = :type ORDER BY timestamp DESC")
    fun getReadingsByType(profileId: Long, type: HealthReadingType): Flow<List<HealthReading>>

    @Query("SELECT * FROM health_readings WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getAllReadingsForProfile(profileId: Long): Flow<List<HealthReading>>

    @Query("SELECT * FROM health_readings WHERE profileId = :profileId AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getReadingsSince(profileId: Long, sinceTimestamp: Long): List<HealthReading>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: HealthReading): Long

    @Delete
    suspend fun deleteReading(reading: HealthReading)
}

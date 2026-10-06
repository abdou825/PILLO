package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE scheduledTime BETWEEN :startTime AND :endTime ORDER BY scheduledTime ASC")
    fun getLogsForRange(startTime: Long, endTime: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE profileId = :profileId AND scheduledTime BETWEEN :startTime AND :endTime ORDER BY scheduledTime ASC")
    fun getLogsForProfileAndRange(profileId: Long, startTime: Long, endTime: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId ORDER BY scheduledTime DESC")
    fun getLogsForMedicine(medicineId: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getLogByMedicineAndTime(medicineId: Long, scheduledTime: Long): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): DoseLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLog): Long

    @Update
    suspend fun updateLog(log: DoseLog)

    @Query("UPDATE dose_logs SET status = :status, actionTime = :actionTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DoseStatus, actionTime: Long)

    @Query("DELETE FROM dose_logs WHERE medicineId = :medicineId")
    suspend fun deleteLogsForMedicine(medicineId: Long)
}

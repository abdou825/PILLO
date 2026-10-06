package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Medicine
import com.example.data.model.MedicineStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND isActive = 1 AND status = 'ACTIVE' AND remainingQuantity > 0 ORDER BY id DESC")
    fun getActiveMedicines(profileId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND status = 'ACTIVE' AND remainingQuantity > 0 ORDER BY id DESC")
    fun getActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND (isActive = 0 OR status = 'FINISHED' OR remainingQuantity <= 0) ORDER BY id DESC")
    fun getFinishedMedicines(profileId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE isActive = 0 OR status = 'FINISHED' OR remainingQuantity <= 0 ORDER BY id DESC")
    fun getFinishedMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllMedicinesForProfile(profileId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY id DESC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    fun getMedicineById(id: Long): Flow<Medicine?>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineByIdSync(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND status = 'ACTIVE' AND remainingQuantity > 0")
    suspend fun getActiveMedicinesSync(): List<Medicine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)
}

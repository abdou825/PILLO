package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Certificate
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates WHERE profileId = :profileId ORDER BY id DESC")
    fun getCertificatesForProfile(profileId: Long): Flow<List<Certificate>>

    @Query("SELECT * FROM certificates ORDER BY id DESC")
    fun getAllCertificates(): Flow<List<Certificate>>

    @Query("SELECT COUNT(*) FROM certificates WHERE profileId = :profileId")
    suspend fun getCertificateCount(profileId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: Certificate): Long

    @Delete
    suspend fun deleteCertificate(certificate: Certificate)
}

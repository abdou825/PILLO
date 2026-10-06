package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND dateTime >= :now ORDER BY dateTime ASC")
    fun getUpcomingAppointments(profileId: Long, now: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY dateTime DESC")
    fun getAllAppointments(profileId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime >= :now ORDER BY dateTime ASC")
    suspend fun getAllUpcomingSync(now: Long): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}

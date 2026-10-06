package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_MEDICINE_IDS = "extra_medicine_ids"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    const val EXTRA_IS_TEST = "extra_is_test"
    const val EXTRA_PROFILE_ID = "extra_profile_id"
    const val EXTRA_PROFILE_NAME = "extra_profile_name"
    const val EXTRA_IS_APPOINTMENT = "extra_is_appointment"
    const val EXTRA_APPOINTMENT_TITLE = "extra_appointment_title"

    fun scheduleAlarm(
        context: Context,
        medicineIds: LongArray,
        scheduledTimeMs: Long,
        isTest: Boolean = false,
        profileId: Long = 1L,
        profileName: String = "أنا"
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDICINE_IDS, medicineIds)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTimeMs)
            putExtra(EXTRA_IS_TEST, isTest)
            putExtra(EXTRA_PROFILE_ID, profileId)
            putExtra(EXTRA_PROFILE_NAME, profileName)
            putExtra(EXTRA_IS_APPOINTMENT, false)
        }

        val requestCode = if (isTest) 99999 else (scheduledTimeMs % 1000000).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for AlarmClockInfo
        val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(EXTRA_MEDICINE_IDS, medicineIds)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTimeMs)
            putExtra(EXTRA_IS_TEST, isTest)
            putExtra(EXTRA_PROFILE_ID, profileId)
            putExtra(EXTRA_PROFILE_NAME, profileName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(scheduledTimeMs, showPendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTimeMs, pendingIntent)
            }
        }
    }

    fun scheduleTestAlarm(context: Context, delaySeconds: Int = 10) {
        val triggerTimeMs = System.currentTimeMillis() + (delaySeconds * 1000L)
        scheduleAlarm(
            context = context,
            medicineIds = longArrayOf(-1L),
            scheduledTimeMs = triggerTimeMs,
            isTest = true,
            profileId = 1L,
            profileName = "أنا (تجربة)"
        )
    }

    fun scheduleSnooze(context: Context, medicineIds: LongArray, minutes: Int = 10, profileId: Long = 1L, profileName: String = "") {
        val snoozeTimeMs = System.currentTimeMillis() + (minutes * 60 * 1000L)
        scheduleAlarm(
            context = context,
            medicineIds = medicineIds,
            scheduledTimeMs = snoozeTimeMs,
            isTest = false,
            profileId = profileId,
            profileName = profileName
        )
    }

    /**
     * Requirement: If user skips, re-ring again after 5 minutes!
     */
    fun scheduleSkipReRing(context: Context, medicineIds: LongArray, profileId: Long = 1L, profileName: String = "") {
        val repeatTimeMs = System.currentTimeMillis() + (5 * 60 * 1000L)
        scheduleAlarm(
            context = context,
            medicineIds = medicineIds,
            scheduledTimeMs = repeatTimeMs,
            isTest = false,
            profileId = profileId,
            profileName = profileName
        )
    }

    fun scheduleAppointment(
        context: Context,
        appointmentId: Long,
        triggerTimeMs: Long,
        title: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_IS_APPOINTMENT, true)
            putExtra(EXTRA_APPOINTMENT_TITLE, title)
        }
        val reqCode = (appointmentId * 100 + (triggerTimeMs % 100)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    /**
     * Reschedules all upcoming doses for all active medicines across all family profiles.
     */
    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(context)
            val medicines = database.medicineDao().getActiveMedicinesSync()
            val now = System.currentTimeMillis()

            val profiles = database.userProfileDao().getAllProfiles()
            val profileMap = mutableMapOf<Long, String>()

            val timeMap = mutableMapOf<Long, MutableList<Long>>()
            val timeProfileMap = mutableMapOf<Long, Long>()

            for (med in medicines) {
                if (med.remainingQuantity <= 0 || !med.isActive) continue
                val times = med.customScheduleTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                for (dayOffset in 0..2) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, dayOffset)

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
                            val triggerMs = doseCal.timeInMillis
                            if (triggerMs > now) {
                                val list = timeMap.getOrPut(triggerMs) { mutableListOf() }
                                list.add(med.id)
                                timeProfileMap[triggerMs] = med.profileId
                            }
                        }
                    }
                }
            }

            val sortedTimes = timeMap.keys.sorted().take(48)
            for (triggerMs in sortedTimes) {
                val medIds = timeMap[triggerMs]?.toLongArray() ?: continue
                val profId = timeProfileMap[triggerMs] ?: 1L
                val prof = database.userProfileDao().getProfileByIdSync(profId)
                val profName = prof?.displayName ?: "أنا"

                scheduleAlarm(
                    context = context,
                    medicineIds = medIds,
                    scheduledTimeMs = triggerMs,
                    isTest = false,
                    profileId = profId,
                    profileName = profName
                )
            }

            // Reschedule upcoming appointments (1 day before and 2 hours before)
            val appts = database.appointmentDao().getAllUpcomingSync(now)
            for (app in appts) {
                val oneDayBefore = app.dateTime - (24 * 60 * 60 * 1000L)
                val twoHoursBefore = app.dateTime - (2 * 60 * 60 * 1000L)

                if (oneDayBefore > now) {
                    scheduleAppointment(context, app.id, oneDayBefore, "تذكير: غداً ميعاد ${app.title}")
                }
                if (twoHoursBefore > now) {
                    scheduleAppointment(context, app.id, twoHoursBefore, "تذكير: بعد ساعتين ميعاد ${app.title}")
                }
            }
        }
    }
}

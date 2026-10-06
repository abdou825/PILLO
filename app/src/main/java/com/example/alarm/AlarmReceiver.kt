package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val medicineIds = intent?.getLongArrayExtra(AlarmScheduler.EXTRA_MEDICINE_IDS) ?: longArrayOf()
        val scheduledTime = intent?.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis()) ?: System.currentTimeMillis()
        val isTest = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_TEST, false) ?: false

        // 1. Start persistent Foreground Service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmScheduler.EXTRA_MEDICINE_IDS, medicineIds)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(AlarmScheduler.EXTRA_IS_TEST, isTest)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // 2. Launch full-screen alert Activity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_MEDICINE_IDS, medicineIds)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(AlarmScheduler.EXTRA_IS_TEST, isTest)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        try {
            context.startActivity(alertIntent)
        } catch (_: Exception) {}
    }
}

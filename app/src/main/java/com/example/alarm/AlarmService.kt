package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.DoseStatus
import com.example.data.repository.PilloRepository
import com.example.media.VoiceAssistantHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmService : Service() {
    companion object {
        const val CHANNEL_ID = "pillo_alarm_channel"
        const val CHANNEL_NAME = "تنبيهات أدوية بيلّو"
        const val MISSED_CHANNEL_ID = "pillo_missed_channel"
        const val MISSED_CHANNEL_NAME = "الجرعات الفائتة"
        const val APPOINTMENT_CHANNEL_ID = "pillo_appointment_channel"
        const val APPOINTMENT_CHANNEL_NAME = "المواعيد والفحوصات"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_ALARM = "com.example.pillo.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.pillo.ACTION_STOP_ALARM"
        const val ACTION_TAKE_DOSE = "com.example.pillo.ACTION_TAKE_DOSE"
        const val ACTION_SNOOZE_DOSE = "com.example.pillo.ACTION_SNOOZE_DOSE"
        const val ACTION_SKIP_DOSE = "com.example.pillo.ACTION_SKIP_DOSE"

        @Volatile
        var isRinging = false
            private set
    }

    private lateinit var soundPlayer: SoundPlayer
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var timeoutJob: Job? = null
    private var voiceAssistant: VoiceAssistantHelper? = null

    private var currentMedicineIds: LongArray = longArrayOf()
    private var currentScheduledTime: Long = 0L
    private var currentIsTest: Boolean = false
    private var currentProfileId: Long = 1L
    private var currentProfileName: String = ""

    override fun onCreate() {
        super.onCreate()
        soundPlayer = SoundPlayer(this)
        createNotificationChannels()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Pillo:AlarmWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(35 * 60 * 1000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isAppointment = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_APPOINTMENT, false) ?: false
        if (isAppointment) {
            val title = intent?.getStringExtra(AlarmScheduler.EXTRA_APPOINTMENT_TITLE) ?: "ميعادك الطبي"
            showAppointmentNotification(title)
            return START_NOT_STICKY
        }

        val action = intent?.action ?: ACTION_START_ALARM

        when (action) {
            ACTION_STOP_ALARM -> {
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_TAKE_DOSE -> {
                handleTakeDose()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE_DOSE -> {
                handleSnoozeDose()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_SKIP_DOSE -> {
                handleSkipDose()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_START_ALARM -> {
                currentMedicineIds = intent?.getLongArrayExtra(AlarmScheduler.EXTRA_MEDICINE_IDS) ?: longArrayOf()
                currentScheduledTime = intent?.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis()) ?: System.currentTimeMillis()
                currentIsTest = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_TEST, false) ?: false
                currentProfileId = intent?.getLongExtra(AlarmScheduler.EXTRA_PROFILE_ID, 1L) ?: 1L
                currentProfileName = intent?.getStringExtra(AlarmScheduler.EXTRA_PROFILE_NAME) ?: ""

                startForegroundWithNotification()
                playAlarm()
                startTimeouts()
            }
        }

        return START_STICKY
    }

    private fun playAlarm() {
        isRinging = true
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            val profile = db.userProfileDao().getProfileByIdSync(currentProfileId)
                ?: db.userProfileDao().getUserProfileSync()

            val soundStyle = profile?.alarmSoundIndex ?: 0
            soundPlayer.startAlarm(soundStyle)

            // Feature 16: Voice Alert Announcement if enabled
            if (profile?.voiceAlertEnabled == true) {
                voiceAssistant = VoiceAssistantHelper(applicationContext, {}, {})
                val displayName = profile.displayName
                val message = if (profile.isOwner) {
                    "ميعاد جرعة علاجك يا بطل! خده دلوقتي."
                } else {
                    "ميعاد دوا $displayName! يرجى التأكيد."
                }
                delay(2000)
                voiceAssistant?.speakArabic(message)
            }
        }
    }

    private fun startForegroundWithNotification() {
        val fullScreenIntent = Intent(this, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_MEDICINE_IDS, currentMedicineIds)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, currentScheduledTime)
            putExtra(AlarmScheduler.EXTRA_IS_TEST, currentIsTest)
            putExtra(AlarmScheduler.EXTRA_PROFILE_ID, currentProfileId)
            putExtra(AlarmScheduler.EXTRA_PROFILE_NAME, currentProfileName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            100,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_TAKE_DOSE
        }
        val takePendingIntent = PendingIntent.getService(
            this,
            101,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_DOSE
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            102,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerText = when {
            currentIsTest -> "تجربة منبه بيلّو (بصوت عالي)"
            currentProfileName.isNotBlank() && !currentProfileName.contains("أنا") -> "ميعاد دوا $currentProfileName! خده دلوقتي"
            else -> "ميعاد الدوا يا بطل! خده دلوقتي"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pillo_icon)
            .setContentTitle(headerText)
            .setContentText("اضغط لتأكيد أخذ الجرعة أو تأجيلها")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, "أخدت الدوا", takePendingIntent)
            .addAction(0, "فكّرني بعد 10 دقايق", snoozePendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startTimeouts() {
        timeoutJob?.cancel()
        timeoutJob = serviceScope.launch {
            val maxDurationMs = 30 * 60 * 1000L
            delay(maxDurationMs)

            if (isActive && isRinging) {
                handleMissedDose()
                showMissedNotification()
                stopForegroundAndSelf()
            }
        }
    }

    private fun handleTakeDose() {
        if (currentIsTest) return
        val db = AppDatabase.getInstance(applicationContext)
        val repo = PilloRepository(db)
        val timeLabel = SimpleDateFormat("HH:mm", Locale.US).format(Date(currentScheduledTime))
        CoroutineScope(Dispatchers.IO).launch {
            for (id in currentMedicineIds) {
                if (id > 0) {
                    repo.markDoseTaken(id, currentScheduledTime, timeLabel)
                }
            }
            AlarmScheduler.rescheduleAll(applicationContext)
        }
    }

    private fun handleSnoozeDose() {
        AlarmScheduler.scheduleSnooze(applicationContext, currentMedicineIds, 10, currentProfileId, currentProfileName)
    }

    /**
     * Requirement: If user presses Skip, re-ring after 5 minutes!
     * ("ولو داس تخطى بعد خمس دقايق يرن تانى")
     */
    private fun handleSkipDose() {
        if (!currentIsTest) {
            val db = AppDatabase.getInstance(applicationContext)
            val repo = PilloRepository(db)
            val timeLabel = SimpleDateFormat("HH:mm", Locale.US).format(Date(currentScheduledTime))
            CoroutineScope(Dispatchers.IO).launch {
                for (id in currentMedicineIds) {
                    if (id > 0) {
                        repo.markDoseSkipped(id, currentScheduledTime, timeLabel)
                    }
                }
            }
            // Re-ring again after 5 minutes
            AlarmScheduler.scheduleSkipReRing(applicationContext, currentMedicineIds, currentProfileId, currentProfileName)
        }
    }

    private fun handleMissedDose() {
        if (currentIsTest) return
        val db = AppDatabase.getInstance(applicationContext)
        val repo = PilloRepository(db)
        val timeLabel = SimpleDateFormat("HH:mm", Locale.US).format(Date(currentScheduledTime))
        CoroutineScope(Dispatchers.IO).launch {
            for (id in currentMedicineIds) {
                if (id > 0) {
                    repo.markDoseMissed(id, currentScheduledTime, timeLabel)
                }
            }
            AlarmScheduler.rescheduleAll(applicationContext)
        }
    }

    private fun showMissedNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val missedNotification = NotificationCompat.Builder(this, MISSED_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pillo_icon)
            .setContentTitle("فاتتك جرعة")
            .setContentText("تنبيه: ميعاد جرعة العلاج عدى بدون تأكيد. راجع دكتورك ومواعيدك دايماً.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(2002, missedNotification)
    }

    private fun showAppointmentNotification(title: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(this, APPOINTMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pillo_icon)
            .setContentTitle("تذكير موعد طبي 🗓️")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(3002, notif)
    }

    private fun stopForegroundAndSelf() {
        isRinging = false
        timeoutJob?.cancel()
        soundPlayer.stopAlarm()
        voiceAssistant?.destroy()
        voiceAssistant = null
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundAndSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة تنبيهات الدواء بصوت مرتفع وشاشة كاملة"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
            }

            val missedChannel = NotificationChannel(
                MISSED_CHANNEL_ID,
                MISSED_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الجرعات الفائتة"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val apptChannel = NotificationChannel(
                APPOINTMENT_CHANNEL_ID,
                APPOINTMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات مواعيد الكشف والتحاليل الطبية"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(missedChannel)
            notificationManager.createNotificationChannel(apptChannel)
        }
    }
}

package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.SoundPlayer
import com.example.data.model.AgeMode
import com.example.data.model.UserProfile
import com.example.ui.components.TimeStepperField
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onTestAlarm: (delaySeconds: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val soundPlayer = remember { SoundPlayer(context) }

    var ageMode by remember { mutableStateOf(userProfile.ageMode) }
    var wakeTime by remember { mutableStateOf(userProfile.wakeTime) }
    var sleepTime by remember { mutableStateOf(userProfile.sleepTime) }
    var breakfastTime by remember { mutableStateOf(userProfile.breakfastTime) }
    var lunchTime by remember { mutableStateOf(userProfile.lunchTime) }
    var dinnerTime by remember { mutableStateOf(userProfile.dinnerTime) }
    var alarmSoundIndex by remember { mutableIntStateOf(userProfile.alarmSoundIndex) }
    var snoozeMinutes by remember { mutableIntStateOf(userProfile.snoozeMinutes) }
    var parentPin by remember { mutableStateOf(userProfile.parentPin) }

    // Test alarm countdown feedback
    var testCountdown by remember { mutableIntStateOf(0) }
    var isPreviewingSound by remember { mutableStateOf(false) }

    // Permissions check
    var hasExactAlarm by remember { mutableStateOf(true) }
    var hasBatteryExemption by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            hasExactAlarm = alarmManager?.canScheduleExactAlarms() ?: true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            hasBatteryExemption = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        }
    }

    LaunchedEffect(testCountdown) {
        if (testCountdown > 0) {
            delay(1000)
            testCountdown--
        }
    }

    // Parent PIN dialog for Kids mode protection
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pendingAgeMode by remember { mutableStateOf<AgeMode?>(null) }
    var pinError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات بيلّو", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QA TOOL SECTION (Brief Feature 8)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أداة الاختبار السريع (QA Tool)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1D4ED8)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط الزر، واقفل الشاشة أو سيبها؛ المنبه هيرن بأعلى صوت مع شاشة التنبيه بعد 10 ثواني.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1E40AF)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            testCountdown = 10
                            onTestAlarm(10)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("qa_test_alarm_button")
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (testCountdown > 0) "المنبه هيرن بعد $testCountdown ثواني..." else "جرّب المنبه بعد 10 ثواني",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 1. Age Mode
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "نمط الاستخدام والسن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "بيغير أحجام الخطوط، الألوان، وأزرار شاشة التنبيه.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AgeMode.values().forEach { mode ->
                        val isSelected = mode == ageMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (userProfile.ageMode == AgeMode.KIDS && mode != AgeMode.KIDS) {
                                        pendingAgeMode = mode
                                        showPinDialog = true
                                    } else {
                                        ageMode = mode
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${mode.labelArabic} (${mode.ageRangeArabic})",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 2. Alarm Sounds Selector (3 Loud Built-in Sounds)
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "صوت ونغمة المنبه (3 نغمات فائقة العلو)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val soundNames = listOf(
                        "1. النبض القوي الترددي (الأعلى تأثيراً)",
                        "2. التنبيه الثنائي العاجل",
                        "3. النغمات الثلاثية المتتابعة"
                    )

                    soundNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    alarmSoundIndex = index
                                    soundPlayer.stopAlarm()
                                    isPreviewingSound = false
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (alarmSoundIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (alarmSoundIndex == index) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            if (isPreviewingSound) {
                                soundPlayer.stopAlarm()
                                isPreviewingSound = false
                            } else {
                                soundPlayer.startAlarm(alarmSoundIndex)
                                isPreviewingSound = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (isPreviewingSound) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPreviewingSound) "إيقاف المعاينة" else "سماع النغمة المختارة")
                    }
                }
            }

            // 3. Snooze Duration
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مدة التأجيل (الغفوة)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15).forEach { mins ->
                            FilterChip(
                                selected = snoozeMinutes == mins,
                                onClick = { snoozeMinutes = mins },
                                label = { Text("$mins دقايق") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Daily Routine Times
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مواعيد روتينك اليومي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "تعديل هذه الأوقات يعيد جدولة كل مواعيد أدوية اليوم تلقائياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TimeStepperField(label = "الصحيان", value = wakeTime, onValueChange = { wakeTime = it })
                    TimeStepperField(label = "الفطار", value = breakfastTime, onValueChange = { breakfastTime = it })
                    TimeStepperField(label = "الغدا", value = lunchTime, onValueChange = { lunchTime = it })
                    TimeStepperField(label = "العشا", value = dinnerTime, onValueChange = { dinnerTime = it })
                    TimeStepperField(label = "النوم", value = sleepTime, onValueChange = { sleepTime = it })
                }
            }

            // 5. Permissions Checklist & Fix
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حالة أذونات المنبه",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Exact Alarm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المنبه الدقيق (Exact Alarms):")
                        if (hasExactAlarm) {
                            Text("مفعل ✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    }
                                }
                            ) { Text("إصلاح") }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Battery Exemption
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("استثناء البطارية (Doze):")
                        if (hasBatteryExemption) {
                            Text("مفعل ✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    }
                                }
                            ) { Text("إصلاح") }
                        }
                    }
                }
            }

            // 6. Parent PIN
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الرمز السري للوالدين (PIN)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "لحماية تعديل الجرعات والإعدادات في نمط الأطفال.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = parentPin,
                        onValueChange = { if (it.length <= 4) parentPin = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        label = { Text("رمز PIN (4 أرقام)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Settings
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val updated = userProfile.copy(
                        ageMode = ageMode,
                        wakeTime = wakeTime,
                        sleepTime = sleepTime,
                        breakfastTime = breakfastTime,
                        lunchTime = lunchTime,
                        dinnerTime = dinnerTime,
                        alarmSoundIndex = alarmSoundIndex,
                        snoozeMinutes = snoozeMinutes,
                        parentPin = parentPin.ifBlank { "1234" }
                    )
                    soundPlayer.stopAlarm()
                    onSaveProfile(updated)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("حفظ التغييرات ومزامنة المواعيد", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Parent PIN Verification Dialog
    if (showPinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pendingAgeMode = null
                pinError = false
            },
            title = { Text("رمز أمان الوالدين") },
            text = {
                Column {
                    Text("أدخل رمز PIN لتغيير النمط أو الإعدادات:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        isError = pinError,
                        label = { Text("PIN") }
                    )
                    if (pinError) {
                        Text("الرمز غير صحيح (الافتراضي 1234)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (enteredPin == parentPin) {
                            if (pendingAgeMode != null) {
                                ageMode = pendingAgeMode!!
                            }
                            showPinDialog = false
                            enteredPin = ""
                        } else {
                            pinError = true
                        }
                    }
                ) { Text("تأكيد") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    focusManager.clearFocus()
                    showPinDialog = false
                    enteredPin = ""
                }) { Text("إلغاء") }
            }
        )
    }
}

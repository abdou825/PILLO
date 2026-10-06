package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeMode
import com.example.data.model.DoseStatus
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import com.example.ui.components.EndOfTreatmentDialog
import com.example.ui.components.KidsCharacterView
import com.example.ui.components.MotivationalAiDialog
import com.example.ui.components.MedicineCounterVisual
import com.example.ui.components.RamadanTravelDialog
import com.example.ui.components.VoiceAssistantDialog
import com.example.ui.theme.getStyleForAgeMode
import com.example.ui.viewmodel.TodayDoseItem
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    todayDoses: List<TodayDoseItem>,
    activeMedicines: List<Medicine>,
    finishedMedicines: List<Medicine>,
    lowStockMedicines: List<Medicine>,
    onAddMedicine: () -> Unit,
    onEditMedicine: (Medicine) -> Unit,
    onTakeDose: (medicineId: Long, scheduledTimeMs: Long, timeLabel: String) -> Unit,
    onReplenishMedicine: (medicineId: Long, additionalQuantity: Int) -> Unit,
    onFinishMedicine: (medicineId: Long) -> Unit,
    onCompleteTreatment: (Medicine) -> Unit,
    onExtendTreatment: (Medicine, extraQuantity: Int) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onNavigateHealthLog: () -> Unit,
    onNavigateCertificates: () -> Unit,
    onNavigateAppointments: () -> Unit,
    onNavigateFamily: () -> Unit,
    onNavigateMiniGames: () -> Unit,
    onNavigatePet: () -> Unit,
    onNavigateSocial: () -> Unit,
    onNavigateDigitalIdCard: () -> Unit = {},
    onNavigateEmergency: () -> Unit,
    onNavigateSettings: () -> Unit,
    onTestAlarm: () -> Unit
) {
    val context = LocalContext.current
    val ageMode = userProfile.ageMode
    val style = getStyleForAgeMode(ageMode)
    val isSenior = ageMode == AgeMode.SENIORS
    val isKids = ageMode == AgeMode.KIDS

    var selectedTab by remember { mutableIntStateOf(0) }
    var replenishMedicineTarget by remember { mutableStateOf<Medicine?>(null) }
    var replenishAmountText by remember { mutableStateOf("20") }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showRamadanDialog by remember { mutableStateOf(false) }
    var showMotivationalAiDialog by remember { mutableStateOf(false) }
    var endOfTreatmentTarget by remember { mutableStateOf<Medicine?>(null) }

    // Check if critical permissions are granted
    var hasExactAlarmPermission by remember { mutableStateOf(true) }
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            hasExactAlarmPermission = alarmManager?.canScheduleExactAlarms() ?: true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "بيلّو",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${ageMode.labelArabic}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                actions = {
                    // Motivational AI Coach Icon
                    IconButton(
                        onClick = { showMotivationalAiDialog = true },
                        modifier = Modifier.testTag("home_motivational_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "كوتش بيلّو الذكي للتحفيز",
                            tint = Color(0xFFA855F7)
                        )
                    }

                    // Voice Assistant Icon
                    IconButton(
                        onClick = { showVoiceDialog = true },
                        modifier = Modifier.testTag("home_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "أوامر صوتية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Ramadan & Travel Schedule Icon
                    IconButton(
                        onClick = { showRamadanDialog = true },
                        modifier = Modifier.testTag("home_ramadan_travel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "رمضان والسفر",
                            tint = Color(0xFF8B5CF6)
                        )
                    }

                    // Digital ID Card Icon
                    IconButton(
                        onClick = onNavigateDigitalIdCard,
                        modifier = Modifier.testTag("home_digital_id_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "بطاقة الهوية الرقمية",
                            tint = Color(0xFFD97706)
                        )
                    }

                    // Quick Emergency Card Icon
                    IconButton(
                        onClick = onNavigateEmergency,
                        modifier = Modifier.testTag("home_emergency_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "بطاقة الطوارئ",
                            tint = Color(0xFFDC2626)
                        )
                    }

                    // Settings Icon
                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* stay on home */ },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "الجرعات") },
                    label = { Text("الجرعات") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateHealthLog,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "صحتي") },
                    label = { Text("صحتي") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateCertificates,
                    icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "الشهادات") },
                    label = { Text("الشهادات") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateAppointments,
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "المواعيد") },
                    label = { Text("المواعيد") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateFamily,
                    icon = { Icon(Icons.Default.Groups, contentDescription = "العائلة") },
                    label = { Text("العائلة") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("home_add_medicine_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة دواء")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة دواء", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // User Profile Greeting Header Bar
            val greetingName = userProfile.displayName.ifEmpty { "أحمد البطل" }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateSocial() }
                    .testTag("home_user_greeting_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (userProfile.avatarId == "avatar_2") "🦁" else if (userProfile.avatarId == "avatar_3") "👑" else "🧑‍⚕️",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أهلاً يا $greetingName 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (userProfile.isGoogleLinked) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🌐 حساب Google متصل",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Motivational AI Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .clickable { showMotivationalAiDialog = true }
                    .testTag("home_motivational_ai_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9D5FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "كوتش بيلّو: \"أنت قدها يا $greetingName! خطوة جديدة لصحتك 🌟\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B21A8)
                        )
                    }
                    Text("تشجيع ⚡", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
                }
            }

            // Kids Hero Character Greeting
            if (isKids) {
                KidsCharacterView()
            }

            // Phase 3 Fun & Social Quick Access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigatePet() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐾 أليفك", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateMiniGames() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎮 الألعاب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E40AF))
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { onNavigateSocial() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪪 بطاقتي والتحدي", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                    }
                }
            }

            // FEATURE 1: Permission Warning Banner
            if (!hasExactAlarmPermission || !isIgnoringBatteryOptimizations) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("permission_warning_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "التنبيه ممكن ميرنش بدون الصلاحيات دي",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = onNavigateSettings,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تفعيل", fontSize = 12.sp)
                        }
                    }
                }
            }

            // FEATURE 5: Low-Stock Warning Banners
            lowStockMedicines.forEach { lowMed ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("low_stock_banner_${lowMed.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "علاجك (${lowMed.name}) قرب يخلص، فاضل يومين",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            )
                        }

                        Text(
                            text = "راجع دكتورك قبل ما توقف أي علاج.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F)),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    replenishMedicineTarget = lowMed
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("هكمّل (إضافة علبة)")
                            }

                            OutlinedButton(
                                onClick = { endOfTreatmentTarget = lowMed },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("هخلّصه")
                            }
                        }
                    }
                }
            }

            // Tabs: جرعات اليوم vs كل الأدوية
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "جرعات اليوم (${todayDoses.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isSenior) 22.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "الأدوية الحالية (${activeMedicines.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isSenior) 22.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
                if (finishedMedicines.isNotEmpty()) {
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "الأدوية المنتهية (${finishedMedicines.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = if (isSenior) 22.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> TodayDosesList(
                    doses = todayDoses,
                    isSenior = isSenior,
                    onTakeDose = onTakeDose,
                    onEditMedicine = onEditMedicine
                )
                1 -> ActiveMedicinesList(
                    medicines = activeMedicines,
                    isSenior = isSenior,
                    onEditMedicine = onEditMedicine,
                    onReplenish = { replenishMedicineTarget = it },
                    onFinish = { med -> endOfTreatmentTarget = med }
                )
                2 -> FinishedMedicinesList(
                    medicines = finishedMedicines,
                    isSenior = isSenior,
                    onReplenish = { replenishMedicineTarget = it }
                )
            }
        }
    }

    // Voice Assistant Dialog
    if (showVoiceDialog) {
        VoiceAssistantDialog(
            onTakeNextDose = {
                val next = todayDoses.firstOrNull { it.status == DoseStatus.PENDING }
                if (next != null) {
                    onTakeDose(next.medicine.id, next.scheduledTimeMs, next.timeLabel)
                }
            },
            onSnooze = {
                onTestAlarm()
            },
            onRemainingQuery = {
                // Queries remaining
            },
            onDismiss = { showVoiceDialog = false }
        )
    }

    // Motivational AI Coach Dialog
    if (showMotivationalAiDialog) {
        MotivationalAiDialog(
            userName = userProfile.displayName.ifEmpty { "يا بطل" },
            streakDays = 14,
            activeMedCount = activeMedicines.size,
            onDismiss = { showMotivationalAiDialog = false }
        )
    }

    // Ramadan & Travel Schedule Dialog
    if (showRamadanDialog) {
        RamadanTravelDialog(
            userProfile = userProfile,
            onSaveProfile = { updated ->
                onSaveProfile(updated)
                showRamadanDialog = false
            },
            onDismiss = { showRamadanDialog = false }
        )
    }

    // End of Treatment Celebration & Extension Dialog
    if (endOfTreatmentTarget != null) {
        val targetMed = endOfTreatmentTarget!!
        EndOfTreatmentDialog(
            medicine = targetMed,
            adherencePercent = 96,
            onFinishTreatment = { med ->
                onCompleteTreatment(med)
                endOfTreatmentTarget = null
            },
            onExtendTreatment = { med, extraQty ->
                onExtendTreatment(med, extraQty)
                endOfTreatmentTarget = null
            },
            onDismiss = { endOfTreatmentTarget = null }
        )
    }

    // Replenish / Add Quantity Dialog
    if (replenishMedicineTarget != null) {
        val target = replenishMedicineTarget!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { replenishMedicineTarget = null },
            title = { Text("إضافة كمية لـ ${target.name}") },
            text = {
                Column {
                    Text("كم جرعة جديدة هتضيفها؟")
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = replenishAmountText,
                        onValueChange = { replenishAmountText = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        label = { Text("عدد الجرعات") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = replenishAmountText.toIntOrNull() ?: 20
                        onReplenishMedicine(target.id, amount)
                        replenishMedicineTarget = null
                    }
                ) {
                    Text("إضافة وتجديد")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { replenishMedicineTarget = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun TodayDosesList(
    doses: List<TodayDoseItem>,
    isSenior: Boolean,
    onTakeDose: (medicineId: Long, scheduledTimeMs: Long, timeLabel: String) -> Unit,
    onEditMedicine: (Medicine) -> Unit
) {
    if (doses.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "مفيش جرعات مجدولة لليوم!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اضغط على زر (إضافة دواء) بالأسفل للبدء.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(doses) { doseItem ->
                TodayDoseCard(
                    doseItem = doseItem,
                    isSenior = isSenior,
                    onTake = { onTakeDose(doseItem.medicine.id, doseItem.scheduledTimeMs, doseItem.timeLabel) },
                    onClick = { onEditMedicine(doseItem.medicine) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TodayDoseCard(
    doseItem: TodayDoseItem,
    isSenior: Boolean,
    onTake: () -> Unit,
    onClick: () -> Unit
) {
    val isTaken = doseItem.status == DoseStatus.TAKEN
    val isNext = doseItem.isNext

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .testTag("dose_card_${doseItem.medicine.id}_${doseItem.timeLabel}"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isTaken -> Color(0xFFF0FDF4)
                isNext -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isNext) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isTaken) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = doseItem.timeLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "الجرعة القادمة ⏳",
                                color = Color(0xFFB45309),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                if (isTaken) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "أخدت الدوا ✓",
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = doseItem.medicine.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (isSenior) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${doseItem.medicine.doseAmount} • ${doseItem.medicine.mealRelation.labelArabic}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Visual Counter Component
            MedicineCounterVisual(
                medicine = doseItem.medicine,
                bodyFontSize = if (isSenior) 20 else 14
            )

            if (!isTaken) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onTake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSenior) 64.dp else 48.dp)
                        .testTag("home_take_dose_button_${doseItem.medicine.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSenior) Color(0xFF0038A8) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أخدت الدوا",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isSenior) 24.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveMedicinesList(
    medicines: List<Medicine>,
    isSenior: Boolean,
    onEditMedicine: (Medicine) -> Unit,
    onReplenish: (Medicine) -> Unit,
    onFinish: (Medicine) -> Unit
) {
    if (medicines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("مفيش أدوية مسجلة حالياً.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(medicines) { med ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onEditMedicine(med) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = if (isSenior) 24.sp else 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${med.timesPerDay} مرات/يوم",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "المواعيد: ${med.customScheduleTimes}", style = MaterialTheme.typography.bodySmall)

                        MedicineCounterVisual(medicine = med)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReplenish(med) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إضافة علبة")
                            }

                            OutlinedButton(
                                onClick = { onFinish(med) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إنهاء العلاج")
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FinishedMedicinesList(
    medicines: List<Medicine>,
    isSenior: Boolean,
    onReplenish: (Medicine) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(medicines) { med ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "العلاج خلص تماماً ✓",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onReplenish(med) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تجديد العلاج وإعادة التفعيل")
                    }
                }
            }
        }
    }
}

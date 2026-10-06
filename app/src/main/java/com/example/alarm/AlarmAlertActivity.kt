package com.example.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.AppDatabase
import com.example.data.model.AgeMode
import com.example.data.model.Medicine
import com.example.data.model.MedicineType
import com.example.media.VoiceAssistantHelper
import com.example.ui.components.KidsCharacterView
import com.example.ui.components.MedicineCounterVisual
import com.example.ui.theme.PilloTheme
import com.example.ui.theme.getStyleForAgeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmAlertActivity : ComponentActivity() {

    private var voiceAssistant: VoiceAssistantHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val medicineIds = intent.getLongArrayExtra(AlarmScheduler.EXTRA_MEDICINE_IDS) ?: longArrayOf()
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val isTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_TEST, false)
        val profileId = intent.getLongExtra(AlarmScheduler.EXTRA_PROFILE_ID, 1L)
        val profileName = intent.getStringExtra(AlarmScheduler.EXTRA_PROFILE_NAME) ?: ""

        setContent {
            var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
            var ageMode by remember { mutableStateOf(AgeMode.YOUNG_ADULTS) }
            var triggerPopAnimation by remember { mutableStateOf(false) }
            var showSuccessFeedback by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val db = AppDatabase.getInstance(applicationContext)
                val profile = db.userProfileDao().getProfileByIdSync(profileId)
                    ?: db.userProfileDao().getUserProfileSync()
                ageMode = profile?.ageMode ?: AgeMode.YOUNG_ADULTS

                if (isTest) {
                    medicines = listOf(
                        Medicine(
                            id = -1,
                            name = "بانادول (جرعة تجريبية)",
                            type = MedicineType.PILLS,
                            totalQuantity = 10,
                            remainingQuantity = 7,
                            doseAmount = "1 قرص",
                            timesPerDay = 2,
                            mealRelation = com.example.data.model.MealRelation.AFTER_MEAL,
                            customScheduleTimes = "08:00,20:00"
                        )
                    )
                } else {
                    val list = mutableListOf<Medicine>()
                    for (id in medicineIds) {
                        val m = db.medicineDao().getMedicineByIdSync(id)
                        if (m != null) list.add(m)
                    }
                    if (list.isEmpty()) {
                        list.add(
                            Medicine(
                                id = -2,
                                name = "جرعة الدواء المحددة",
                                type = MedicineType.PILLS,
                                totalQuantity = 10,
                                remainingQuantity = 5,
                                doseAmount = "الجرعة المقررة",
                                timesPerDay = 1,
                                mealRelation = com.example.data.model.MealRelation.ANY_TIME,
                                customScheduleTimes = ""
                            )
                        )
                    }
                    medicines = list
                }
            }

            fun executeTake() {
                triggerPopAnimation = true
                showSuccessFeedback = true
                coroutineScope.launch {
                    delay(600)
                    val serviceIntent = Intent(this@AlarmAlertActivity, AlarmService::class.java).apply {
                        action = AlarmService.ACTION_TAKE_DOSE
                    }
                    startService(serviceIntent)
                    delay(500)
                    finish()
                }
            }

            fun executeSnooze(minutes: Int = 10) {
                val serviceIntent = Intent(this@AlarmAlertActivity, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_SNOOZE_DOSE
                }
                startService(serviceIntent)
                finish()
            }

            fun executeSkip() {
                Toast.makeText(context, "تم تخطي الجرعة. هيرن تاني بعد 5 دقايق للتذكير!", Toast.LENGTH_LONG).show()
                val serviceIntent = Intent(this@AlarmAlertActivity, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_SKIP_DOSE
                }
                startService(serviceIntent)
                finish()
            }

            PilloTheme(ageMode = ageMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmAlertContent(
                        medicines = medicines,
                        ageMode = ageMode,
                        isTest = isTest,
                        profileName = profileName,
                        triggerPopAnimation = triggerPopAnimation,
                        showSuccessFeedback = showSuccessFeedback,
                        onTake = { executeTake() },
                        onSnooze = { executeSnooze(10) },
                        onSkip = { executeSkip() },
                        onStartVoiceCommand = {
                            voiceAssistant = VoiceAssistantHelper(
                                context = this@AlarmAlertActivity,
                                onCommandRecognized = { cmd ->
                                    when (cmd) {
                                        is VoiceAssistantHelper.VoiceCommand.TakeDose -> executeTake()
                                        is VoiceAssistantHelper.VoiceCommand.Snooze -> executeSnooze(cmd.minutes)
                                        is VoiceAssistantHelper.VoiceCommand.RemainingQuantityQuery -> {
                                            val firstMed = medicines.firstOrNull()
                                            if (firstMed != null) {
                                                voiceAssistant?.speakArabic("فاضل ${firstMed.remainingQuantity} جرعة من دوا ${firstMed.name}")
                                            }
                                        }
                                        is VoiceAssistantHelper.VoiceCommand.Unrecognized -> {
                                            Toast.makeText(context, "الكلمة: ${cmd.rawText}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                            voiceAssistant?.startListening()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        voiceAssistant?.destroy()
        super.onDestroy()
    }
}

@Composable
fun AlarmAlertContent(
    medicines: List<Medicine>,
    ageMode: AgeMode,
    isTest: Boolean,
    profileName: String,
    triggerPopAnimation: Boolean,
    showSuccessFeedback: Boolean,
    onTake: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onStartVoiceCommand: () -> Unit
) {
    val style = getStyleForAgeMode(ageMode)
    val isSenior = ageMode == AgeMode.SENIORS
    val isKids = ageMode == AgeMode.KIDS

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Alarm Indicator & Alert Headline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "منبه الدواء",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Voice Assistant Button
                    IconButton(
                        onClick = onStartVoiceCommand,
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "أمر صوتي",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alert headline with profile name
                val headline = when {
                    isTest -> "تجربة منبه بيلّو الحقيقي!"
                    profileName.isNotBlank() && !profileName.contains("أنا") -> "ميعاد دوا $profileName يا بطل! خده دلوقتي"
                    else -> "ميعاد الدوا يا بطل! خده دلوقتي"
                }

                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (isSenior) 28.sp else style.headlineFontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                if (isKids) {
                    Spacer(modifier = Modifier.height(8.dp))
                    KidsCharacterView(isCheering = showSuccessFeedback)
                }
            }

            // Middle Section: Medicines info card(s) & Visual Counter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (medicines.size > 1) {
                    Text(
                        text = "عندك ${medicines.size} أدوية في نفس الميعاد:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isSenior) 22.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                medicines.forEach { medicine ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("alert_medicine_card_${medicine.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(style.cardCornerRadiusDp.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = medicine.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = if (isSenior) 26.sp else 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "الجرعة: ${medicine.doseAmount} • ${medicine.mealRelation.labelArabic}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = if (isSenior) 22.sp else style.bodyFontSizeSp.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            MedicineCounterVisual(
                                medicine = medicine,
                                triggerAnimation = triggerPopAnimation,
                                bodyFontSize = if (isSenior) 22 else style.bodyFontSizeSp
                            )
                        }
                    }
                }
            }

            // Bottom Section: Big Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Huge Button: "أخدت الدوا"
                Button(
                    onClick = onTake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSenior) 84.dp else style.buttonMinHeightDp.dp)
                        .testTag("alert_take_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSenior) Color(0xFF0038A8) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(if (isKids) 28.dp else 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSenior) 36.dp else 28.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "أخدت الدوا",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = if (isSenior) 30.sp else 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                if (!isSenior) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button: "فكّرني بعد 10 دقايق"
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("alert_snooze_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "فكّرني بعد 10 دقايق",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        // Button: "هتخطى الجرعة" (rings again in 5 minutes!)
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("alert_skip_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "تخطي (يرن بعد 5 د)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Success Celebration Overlay
        AnimatedVisibility(
            visible = showSuccessFeedback,
            enter = fadeIn(tween(200)) + scaleIn(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (isKids) "برافو يا بطل! كسبت نجمة!" else "تسلم! تم تسجيل الجرعة بنجاح",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = if (isSenior) 26.sp else 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

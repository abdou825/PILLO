package com.example.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeMode
import com.example.data.model.UserProfile
import com.example.ui.components.KidsCharacterView
import com.example.ui.components.TimeStepperField

@Composable
fun OnboardingScreen(
    currentProfile: UserProfile,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedAgeMode by remember { mutableStateOf(currentProfile.ageMode) }
    var wakeTime by remember { mutableStateOf(currentProfile.wakeTime) }
    var sleepTime by remember { mutableStateOf(currentProfile.sleepTime) }
    var breakfastTime by remember { mutableStateOf(currentProfile.breakfastTime) }
    var lunchTime by remember { mutableStateOf(currentProfile.lunchTime) }
    var dinnerTime by remember { mutableStateOf(currentProfile.dinnerTime) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (i == step) 28.dp else 12.dp, 12.dp)
                            .background(
                                color = if (i == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }
            }

            // Animated Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "OnboardingStep"
            ) { currentStep ->
                when (currentStep) {
                    1 -> Step1WelcomeAndSafety(
                        userName = currentProfile.displayName,
                        userEmail = currentProfile.userEmail,
                        isGoogleLinked = currentProfile.isGoogleLinked,
                        onGoogleSignIn = { googleName, googleEmail ->
                            onComplete(
                                currentProfile.copy(
                                    displayName = googleName,
                                    userEmail = googleEmail,
                                    isGoogleLinked = true
                                )
                            )
                        },
                        onManualDetailsChanged = { newName, newEmail ->
                            onComplete(
                                currentProfile.copy(
                                    displayName = newName,
                                    userEmail = newEmail
                                )
                            )
                        }
                    )
                    2 -> Step2ChooseAgeMode(
                        selected = selectedAgeMode,
                        onSelect = { selectedAgeMode = it }
                    )
                    3 -> Step3RoutineTimes(
                        wakeTime = wakeTime,
                        onWakeChange = { wakeTime = it },
                        sleepTime = sleepTime,
                        onSleepChange = { sleepTime = it },
                        breakfastTime = breakfastTime,
                        onBreakfastChange = { breakfastTime = it },
                        lunchTime = lunchTime,
                        onLunchChange = { lunchTime = it },
                        dinnerTime = dinnerTime,
                        onDinnerChange = { dinnerTime = it }
                    )
                    4 -> Step4Permissions(context = context)
                }
            }

            // Navigation Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (step < 4) {
                            step++
                        } else {
                            val updated = currentProfile.copy(
                                ageMode = selectedAgeMode,
                                wakeTime = wakeTime,
                                sleepTime = sleepTime,
                                breakfastTime = breakfastTime,
                                lunchTime = lunchTime,
                                dinnerTime = dinnerTime,
                                isOnboarded = true
                            )
                            onComplete(updated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (step == 4) "ابدأ استخدام بيلّو" else "التالي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (step > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            step--
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("السابق")
                    }
                }
            }
        }
    }
}

@Composable
fun Step1WelcomeAndSafety(
    userName: String = "أحمد البطل",
    userEmail: String = "abdelrahman1atris@gmail.com",
    isGoogleLinked: Boolean = false,
    onGoogleSignIn: (name: String, email: String) -> Unit = { _, _ -> },
    onManualDetailsChanged: (name: String, email: String) -> Unit = { _, _ -> }
) {
    var inputName by remember { mutableStateOf(if (userName == "أنا (المستخدم الرئيسي)") "عبدالرحمن عتريس" else userName) }
    var inputEmail by remember { mutableStateOf(if (userEmail == "hero@pillo.app") "abdelrahman1atris@gmail.com" else userEmail) }
    var googleConnected by remember { mutableStateOf(isGoogleLinked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "أهلاً بيك في بيلّو (Pillo) 👋",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "منبه علاجك الذكي والمصمم مخصوص علشان ميفوتكش ولا ميعاد دوا أبداً.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // GOOGLE SIGN-IN & ACCOUNT CREATION CARD
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (googleConnected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
            ),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (googleConnected) Color(0xFF22C55E) else Color(0xFFCBD5E1)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (googleConnected) "✅ حساب Google متصل" else "🔐 حساب المستخدم",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (googleConnected) Color(0xFF15803D) else Color(0xFF1E293B)
                        )
                    }
                    if (googleConnected) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "مُعتمد رسمياً",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // One-Tap Google Sign-In Button
                Button(
                    onClick = {
                        inputName = "عبدالرحمن عتريس"
                        inputEmail = "abdelrahman1atris@gmail.com"
                        googleConnected = true
                        onGoogleSignIn(inputName, inputEmail)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (googleConnected) Color(0xFF15803D) else Color(0xFFFFFFFF),
                        contentColor = if (googleConnected) Color.White else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!googleConnected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_sign_in_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (googleConnected) "✓ تم تسجيل الدخول بحساب Google" else "🌐 تسجيل الدخول السريع بحساب Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name & Email Fields
                OutlinedTextField(
                    value = inputName,
                    onValueChange = {
                        inputName = it
                        onManualDetailsChanged(inputName, inputEmail)
                    },
                    label = { Text("اسم المستخدم (الاسم الظاهر في الشهادة والبطاقة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = {
                        inputEmail = it
                        onManualDetailsChanged(inputName, inputEmail)
                    },
                    label = { Text("البريد الإلكتروني للربط الأبوي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mandatory Medical Safety Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7) // Warm warning amber
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = "تنبيه أمان طبي مهم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "بيلّو بيفكّرك بس. التزم بتعليمات دكتورك.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }
}

@Composable
fun Step2ChooseAgeMode(
    selected: AgeMode,
    onSelect: (AgeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "مين هيستخدم التطبيق؟",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "بيلّو بيكيف الخطوط والألوان وطريقة التنبيه على حسب السن.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        AgeMode.values().forEach { mode ->
            val isSelected = mode == selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onSelect(mode) }
                    .testTag("age_mode_card_${mode.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.labelArabic,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (mode == AgeMode.SENIORS) 22.sp else 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mode.ageRangeArabic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step3RoutineTimes(
    wakeTime: String,
    onWakeChange: (String) -> Unit,
    sleepTime: String,
    onSleepChange: (String) -> Unit,
    breakfastTime: String,
    onBreakfastChange: (String) -> Unit,
    lunchTime: String,
    onLunchChange: (String) -> Unit,
    dinnerTime: String,
    onDinnerChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "روتين يومك ومواعيد الوجبات",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "بنحسب مواعيد الدوا تلقائياً على أساس أوقات صحيانك وأكلك ونومك.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        TimeStepperField(label = "ميعاد الصحيان", value = wakeTime, onValueChange = onWakeChange)
        TimeStepperField(label = "ميعاد الفطار", value = breakfastTime, onValueChange = onBreakfastChange)
        TimeStepperField(label = "ميعاد الغدا", value = lunchTime, onValueChange = onLunchChange)
        TimeStepperField(label = "ميعاد العشا", value = dinnerTime, onValueChange = onDinnerChange)
        TimeStepperField(label = "ميعاد النوم", value = sleepTime, onValueChange = onSleepChange)
    }
}

@Composable
fun TimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    TimeStepperField(label = label, value = value, onValueChange = onValueChange)
}

@Composable
fun Step4Permissions(context: Context) {
    val postNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "صلاحيات دقة التنبيه",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "علشان المنبه يرن في ميعاده بالثانية حتى لو الموبايل مقفول أو في جيبك.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // 1. Notifications
        PermissionCard(
            title = "إشعارات التنبيه",
            description = "مطلوبة لإظهار تنبيه الجرعة على الشاشة.",
            icon = Icons.Default.Notifications,
            actionLabel = "سماح بالإشعارات",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        // 2. Exact Alarms
        PermissionCard(
            title = "التنبيه الدقيق (بالثانية)",
            description = "مطلوبة لتشغيل المنبه في وقته المضبوط تماماً.",
            icon = Icons.Default.Alarm,
            actionLabel = "ضبط المنبه الدقيق",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
            }
        )

        // 3. Battery Optimizations
        PermissionCard(
            title = "تجاهل توفير البطارية",
            description = "مطلوبة لمنع أندرويد من إيقاف المنبه أثناء وضع الخمول (Doze).",
            icon = Icons.Default.BatteryAlert,
            actionLabel = "استثناء البطارية",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
            }
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionLabel)
            }
        }
    }
}

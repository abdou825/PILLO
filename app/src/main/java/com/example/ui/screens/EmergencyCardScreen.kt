package com.example.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Medicine
import com.example.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCardScreen(
    userProfile: UserProfile,
    activeMedicines: List<Medicine>,
    onSaveProfile: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var bloodType by remember { mutableStateOf(userProfile.bloodType) }
    var allergies by remember { mutableStateOf(userProfile.allergies) }
    var contactName by remember { mutableStateOf(userProfile.emergencyContactName) }
    var contactPhone by remember { mutableStateOf(userProfile.emergencyContactPhone) }
    var persistentNotif by remember { mutableStateOf(userProfile.persistentEmergencyNotification) }

    fun updatePersistentNotification(enabled: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pillo_emergency_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "بطاقة الطوارئ الطبية",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        if (enabled) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "emergency")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                300,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notif = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_pillo_icon)
                .setContentTitle("بطاقة الطوارئ الطبية - بيلّو")
                .setContentText("فصيلة الدم: ${bloodType.ifEmpty { "غير محدد" }} • جهة الاتصال: ${contactName.ifEmpty { "غير محدد" }}")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            manager.notify(3001, notif)
        } else {
            manager.cancel(3001)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بطاقة الطوارئ الطبية", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
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
            // Emergency Header Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFDC2626), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "بطاقة المسعف السريعة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "معلوماتك الحيوية وأدويتك في مكان واحد لأي ظرف طارئ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }

            // Quick Call Button
            if (contactPhone.isNotBlank()) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contactPhone.trim()}"))
                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("emergency_call_button")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "اتصل بالطوارئ ($contactName)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                }
            }

            // Form Fields
            OutlinedTextField(
                value = bloodType,
                onValueChange = { bloodType = it },
                label = { Text("فصيلة الدم") },
                placeholder = { Text("مثال: A+, O-, B+...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("الحساسية من أي أدوية أو أطعمة") },
                placeholder = { Text("مثال: بنسلين، أسبرين، لا يوجد...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("اسم شخص للطوارئ") },
                placeholder = { Text("مثال: أحمد (الأخ)، دكتور طارق...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("رقم هاتف الطوارئ") },
                placeholder = { Text("010xxxxxxx") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Auto-filled Medicines List
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الأدوية الحالية (تلقائية من جدولك):",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (activeMedicines.isEmpty()) {
                        Text("لا توجد أدوية مسجلة حالياً.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        activeMedicines.forEach { med ->
                            Text(
                                text = "• ${med.name} (${med.doseAmount}) - ${med.timesPerDay} مرات/يوم",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Persistent Notification Toggle
            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
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
                            text = "إشعار دائم لبطاقة الطوارئ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "يظهر في شريط الإشعارات دائماً لسرعة الوصول في أي وقت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = persistentNotif,
                        onCheckedChange = {
                            persistentNotif = it
                            updatePersistentNotification(it)
                        }
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val updated = userProfile.copy(
                        bloodType = bloodType.trim(),
                        allergies = allergies.trim(),
                        emergencyContactName = contactName.trim(),
                        emergencyContactPhone = contactPhone.trim(),
                        persistentEmergencyNotification = persistentNotif
                    )
                    onSaveProfile(updated)
                    updatePersistentNotification(persistentNotif)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("emergency_save_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("حفظ بيانات الطوارئ", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

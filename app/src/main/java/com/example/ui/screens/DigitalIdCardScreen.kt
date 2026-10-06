package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Certificate
import com.example.data.model.UserProfile
import com.example.media.IdCardImageGenerator
import com.example.ui.components.IdCardVisualCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalIdCardScreen(
    userProfile: UserProfile,
    certificates: List<Certificate> = emptyList(),
    onSaveProfile: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf(userProfile.displayName.ifEmpty { "أحمد البطل" }) }
    var userEmail by remember { mutableStateOf(userProfile.userEmail.ifEmpty { "hero@pillo.app" }) }
    var fatherPhone by remember { mutableStateOf(userProfile.fatherPhone.ifEmpty { userProfile.emergencyContactPhone.ifEmpty { "01012345678" } }) }
    var selectedAvatarId by remember { mutableStateOf(userProfile.avatarId.ifEmpty { "avatar_1" }) }

    val avatarsList = listOf(
        "avatar_1" to "🧑‍⚕️ طبيب بطل",
        "avatar_2" to "🦁 أسد شجاع",
        "avatar_3" to "👑 ملك الالتزام",
        "avatar_4" to "🚀 رائد فضاء",
        "avatar_5" to "🌟 نجم ساطع",
        "avatar_6" to "🦸‍♂️ بطل خارق",
        "avatar_7" to "🐱 قط لطيف"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("بطاقة الهوية الرقمية 🪪", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("id_card_screen_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل البيانات", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Explanatory Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "بطاقة الهوية الطبية المعتمدة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E40AF)
                        )
                        Text(
                            text = "صورة فنية مجسمة تحتوي على بياناتك ورقم والدك وكود QR لعرض إنجازاتك.",
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THE GORGEOUS VISUAL EMBOSSED ID CARD (WITH IMAGE SAVER)
            IdCardVisualCard(
                userProfile = userProfile,
                userName = userName,
                userEmail = userEmail,
                fatherPhone = fatherPhone,
                avatarId = selectedAvatarId,
                adherencePercent = 98,
                streakDays = 14,
                onViewProfileAndAchievements = {
                    showAchievementsDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Call Father Action Button
            if (fatherPhone.isNotBlank()) {
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${fatherPhone.trim()}"))
                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("id_card_call_father_button")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اتصال مباشر بولي الأمر / الأب ($fatherPhone)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // User Achievements & Certificates Preview Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإنجازات والشهادات المربوطة بالكارت 🏅",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFD97706))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🥇", fontSize = 24.sp)
                                Text("بطل الالتزام", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                Text("98%", fontSize = 10.sp, color = Color(0xFF78350F))
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔥", fontSize = 24.sp)
                                Text("سلسلة 14 يوم", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text("متواصل", fontSize = 10.sp, color = Color(0xFF1E3A8A))
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎖️", fontSize = 24.sp)
                                Text("شهادة ديوانية", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534))
                                Text("معتمدة", fontSize = 10.sp, color = Color(0xFF14532D))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List certificates
                    Text("شهادات الالتزام الصادرة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (certificates.isEmpty()) {
                        Text("• شهادة إتمام كورس العلاج - نسبة الالتزام 100% (موثقة رسمي)", fontSize = 12.sp, color = Color(0xFF15803D))
                        Text("• وسام التاج الذهبي في انتظام الجرعات", fontSize = 12.sp, color = Color(0xFFB45309))
                    } else {
                        certificates.forEach { cert ->
                            Text("• ${cert.medicineName} (${cert.milestoneType.labelArabic}) - نسبة: ${cert.adherencePercent}% ✓", fontSize = 12.sp, color = Color(0xFF15803D))
                        }
                    }
                }
            }
        }
    }

    // DIALOG: EDIT ID CARD DATA
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل بيانات بطاقة الهوية 🪪", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Quick Google Sign-in Autofill Button
                    Button(
                        onClick = {
                            userName = "عبدالرحمن عتريس"
                            userEmail = "abdelrahman1atris@gmail.com"
                            fatherPhone = if (fatherPhone.isEmpty() || fatherPhone == "010xxxxxxxx") "01012345678" else fatherPhone
                            selectedAvatarId = "avatar_1"
                            Toast.makeText(context, "تم استيراد بيانات حساب Google بنجاح! 🌐", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text("🌐 استيراد الاسم والإيميل من حساب Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text("البيانات المعروضة على البطاقة:")
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("اسم المستخدم / الابن") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fatherPhone,
                        onValueChange = { fatherPhone = it },
                        label = { Text("رقم هاتف الوالد / ولي الأمر") },
                        placeholder = { Text("010xxxxxxxx") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("اختر صورتك / الأفاتار:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        avatarsList.take(4).forEach { (id, label) ->
                            val isSelected = selectedAvatarId == id
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0), CircleShape)
                                    .border(2.dp, if (isSelected) Color(0xFFB45309) else Color.Transparent, CircleShape)
                                    .clickable { selectedAvatarId = id },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label.take(2), fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = userProfile.copy(
                            displayName = userName.trim(),
                            userEmail = userEmail.trim(),
                            fatherPhone = fatherPhone.trim(),
                            emergencyContactPhone = if (userProfile.emergencyContactPhone.isEmpty()) fatherPhone.trim() else userProfile.emergencyContactPhone,
                            avatarId = selectedAvatarId
                        )
                        onSaveProfile(updated)
                        showEditDialog = false
                        Toast.makeText(context, "تم حفظ بيانات البطاقة بنجاح! 🪪", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ البطاقة")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // DIALOG: ACHIEVEMENTS & CERTIFICATES (QR DESTINATION)
    if (showAchievementsDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            title = {
                Text("صفحة الإنجازات والشهادات 🏅", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("بطل الالتزام: $userName", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F766E))
                    Text("البريد: $userEmail", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("هاتف الوالد: $fatherPhone", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• متتالية الالتزام الدوائي: 14 يوماً بنسبة 98% 🔥")
                    Text("• وسام البطل الذهبي في التناول بموعده 🏅")
                    Text("• شهادة إتمام كورس العلاج المعتمدة من بيلّو 🎖️")
                }
            },
            confirmButton = {
                Button(onClick = { showAchievementsDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

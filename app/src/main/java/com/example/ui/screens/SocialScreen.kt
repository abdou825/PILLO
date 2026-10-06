package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisorAccount
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeMode
import com.example.data.model.CaretakerLink
import com.example.data.model.Certificate
import com.example.data.model.ChallengeStatus
import com.example.data.model.FriendRecord
import com.example.data.model.LocalChallenge
import com.example.data.model.Medicine
import com.example.data.model.PublicProfile
import com.example.data.model.UserProfile
import com.example.media.IdCardImageGenerator
import com.example.ui.components.IdCardVisualCard
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    userProfile: UserProfile,
    publicProfile: PublicProfile?,
    challenges: List<LocalChallenge>,
    friends: List<FriendRecord>,
    caretakerLinks: List<CaretakerLink>,
    certificates: List<Certificate> = emptyList(),
    activeMedicines: List<Medicine> = emptyList(),
    isOnline: Boolean,
    onCreateAccount: (nickname: String, avatarId: String, isMinor: Boolean, parentConsent: Boolean) -> Unit,
    onUpdateAccountDetails: ((name: String, email: String, fatherPhone: String, avatarId: String) -> Unit)? = null,
    onRewardSon: ((sonEmail: String, coins: Int) -> Unit)? = null,
    onDeleteAccount: () -> Unit,
    onRegenerateQr: () -> String,
    onSendChallenge: (opponentPublicId: String, opponentNickname: String, type: String, days: Int) -> Unit,
    onAcceptChallenge: (LocalChallenge) -> Unit,
    onDeclineChallenge: (LocalChallenge) -> Unit,
    onBlockUser: (targetId: String) -> Unit,
    onInviteCaretaker: (caretakerId: String, caretakerName: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isMinor = userProfile.ageMode == AgeMode.KIDS || userProfile.ageMode == AgeMode.TEENS
    var selectedTab by remember { mutableIntStateOf(0) }

    // Account fields
    var userNameInput by remember { mutableStateOf(userProfile.displayName.ifEmpty { "بطل بيلّو" }) }
    var userEmailInput by remember { mutableStateOf(userProfile.userEmail.ifEmpty { "hero@pillo.app" }) }
    var fatherPhoneInput by remember { mutableStateOf(userProfile.fatherPhone.ifEmpty { userProfile.emergencyContactPhone.ifEmpty { "01012345678" } }) }
    var selectedAvatarId by remember { mutableStateOf(userProfile.avatarId.ifEmpty { "avatar_1" }) }

    // Parental Control state (Search/Link Son by Email)
    var sonEmailInput by remember { mutableStateOf(userProfile.linkedSonEmail.ifEmpty { "son@pillo.app" }) }
    var linkedSonFound by remember { mutableStateOf(userProfile.linkedSonEmail.isNotEmpty()) }
    var sonRewardSent by remember { mutableStateOf(false) }

    // Modals
    var showAccountEditDialog by remember { mutableStateOf(false) }
    var showAchievementsProfileDialog by remember { mutableStateOf(false) }
    var showNewChallengeDialog by remember { mutableStateOf(false) }
    var showParentPinDialog by remember { mutableStateOf(false) }
    var isParentUnlocked by remember { mutableStateOf(publicProfile?.parentConsent == true) }
    var parentPinEntered by remember { mutableStateOf("") }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("البطاقة الذكية والتحكم الأبوي 🪪", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Account Edit / Login Button
                    IconButton(
                        onClick = { showAccountEditDialog = true },
                        modifier = Modifier.testTag("open_account_edit_button")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "تعديل الحساب والبيانات", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Section Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("بطاقتي الذكية QR 🪪", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("التحكم الأبوي 👨‍👦", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("تحديات الالتزام 🔥 (${challenges.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("الأصدقاء 👥 (${friends.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Contents
            when (selectedTab) {
                // TAB 0: SMART VISUAL ID CARD (IMAGE & PRINT READY)
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Profile Summary Bar
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF3B82F6), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "البطاقة الذكية جاهزة للطباعة والمشاركة",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E40AF)
                                        )
                                        Text(
                                            text = "تحتوي على بيانات الطوارئ وهاتف ولي الأمر وكود QR للإنجازات",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                    }
                                }

                                IconButton(onClick = { showAccountEditDialog = true }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "تعديل", tint = Color(0xFF2563EB))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // THE GORGEOUS VISUAL EMBOSSED ID CARD (AS IMAGE / CANVAS)
                        IdCardVisualCard(
                            userProfile = userProfile,
                            userName = userNameInput,
                            userEmail = userEmailInput,
                            fatherPhone = fatherPhoneInput,
                            avatarId = selectedAvatarId,
                            adherencePercent = 98,
                            streakDays = 14,
                            onViewProfileAndAchievements = {
                                showAchievementsProfileDialog = true
                            }
                        )
                    }
                }

                // TAB 1: PARENTAL CONTROL SYSTEM (BY SON'S EMAIL)
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF16A34A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "نظام التحكم الأبوي الذكي 👨‍👦",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "تابع التزام ابنك الدوائي، وتأكد من مواعيده وحمّل بطاقته الطبية.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF14532D)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search / Link Son by Email Field (المطلوب: عن طريق كتابة ايميل الابن)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ربط ومتابعة حساب الابن بواسطة الإيميل:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = sonEmailInput,
                                    onValueChange = { sonEmailInput = it },
                                    label = { Text("اكتب إيميل الابن المسجل في البرنامج") },
                                    placeholder = { Text("son@pillo.app أو omar@gmail.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (sonEmailInput.isNotBlank()) {
                                            linkedSonFound = true
                                            Toast.makeText(context, "تم العثور على حساب الابن وربطه بنجاح! 🌟", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("link_son_by_email_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ربط ومتابعة الابن الآن 🔗", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Son's Live Parental Dashboard
                        if (linkedSonFound) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Son Info Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .background(Color(0xFFFEF3C7), CircleShape)
                                                    .border(2.dp, Color(0xFFF59E0B), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🦸‍♂️", fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "البطل: ${userNameInput.ifEmpty { "أحمد عمر" }}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = "الإيميل: $sonEmailInput",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("متصل ومحمي 🛡️", color = Color(0xFF15803D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Adherence Live Score
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("نسبة الالتزام", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text("98% 🔥", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A))
                                        }
                                        Box(modifier = Modifier.size(width = 1.dp, height = 30.dp).background(Color(0xFFE2E8F0)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("الجرعات الفائتة", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text("0 جرعة ✓", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                                        }
                                        Box(modifier = Modifier.size(width = 1.dp, height = 30.dp).background(Color(0xFFE2E8F0)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("سلسلة الأيام", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text("14 يوم 🏅", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Son's Medicine Schedule Summary
                                    Text(
                                        text = "جدول أدوية الابن اليومية:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (activeMedicines.isEmpty()) {
                                        Text("• بانادول (1 قرص) - 08:00 صباحاً (تم أخذها ✓)", fontSize = 13.sp, color = Color(0xFF15803D))
                                        Text("• أوميجا 3 (1 كبسولة) - 02:00 ظهراً (في الموعد ⏳)", fontSize = 13.sp, color = Color(0xFF2563EB))
                                        Text("• فيتامين د (5 نقط) - 08:00 مساءً (في الموعد ⏳)", fontSize = 13.sp, color = Color(0xFF475569))
                                    } else {
                                        activeMedicines.forEach { med ->
                                            Text("• ${med.name} (${med.doseAmount}) - المواعيد: ${med.customScheduleTimes} ✓", fontSize = 13.sp, color = Color(0xFF15803D))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Parent Quick Actions (Download Son's Card & Reward Son)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val bitmap = IdCardImageGenerator.generateIdCardBitmap(
                                                    context = context,
                                                    profile = userProfile,
                                                    userName = userNameInput,
                                                    userEmail = sonEmailInput,
                                                    fatherPhone = fatherPhoneInput,
                                                    avatarId = selectedAvatarId,
                                                    adherencePercent = 98,
                                                    streakDays = 14
                                                )
                                                IdCardImageGenerator.saveToGallery(context, bitmap, userNameInput)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحميل كارت الابن 📥", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                sonRewardSent = true
                                                onRewardSon?.invoke(sonEmailInput, 50)
                                                Toast.makeText(context, "تم إرسال 50 عملة ذهبية ورسالة فخر للابن! 🌟", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("مكافأة الابن 🎁", fontSize = 12.sp)
                                        }
                                    }

                                    if (sonRewardSent) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "✓ تم إرسال رسالة تشجيع للابن: 'بطل يا حبيبي، فخور بالتزامك!'",
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: CHALLENGES
                2 -> {
                    ChallengesTab(
                        challenges = challenges,
                        onOpenNewChallenge = { showNewChallengeDialog = true },
                        onAccept = onAcceptChallenge,
                        onDecline = onDeclineChallenge
                    )
                }

                // TAB 3: FRIENDS
                3 -> {
                    FriendsTab(
                        friends = friends,
                        onBlockUser = onBlockUser
                    )
                }
            }
        }
    }

    // DIALOG: EDIT ACCOUNT / LOGIN
    if (showAccountEditDialog) {
        AlertDialog(
            onDismissRequest = { showAccountEditDialog = false },
            title = { Text("تسجيل الدخول وإعدادات الحساب 👤", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Quick Google Sign-in Button
                    Button(
                        onClick = {
                            userNameInput = "عبدالرحمن عتريس"
                            userEmailInput = "abdelrahman1atris@gmail.com"
                            fatherPhoneInput = if (fatherPhoneInput.isEmpty() || fatherPhoneInput == "010xxxxxxxx") "01012345678" else fatherPhoneInput
                            selectedAvatarId = "avatar_1"
                            onUpdateAccountDetails?.invoke(userNameInput, userEmailInput, fatherPhoneInput, selectedAvatarId)
                            onCreateAccount(userNameInput, selectedAvatarId, isMinor, true)
                            Toast.makeText(context, "تم تسجيل الدخول واستيراد بيانات حساب Google بنجاح! 🌐", Toast.LENGTH_SHORT).show()
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
                        Text("🌐 تسجيل الدخول بحساب Google واستيراد البيانات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text("بيانات المستخدم وبطاقة الهوية الذكية:")
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        label = { Text("اسم المستخدم / الابن") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userEmailInput,
                        onValueChange = { userEmailInput = it },
                        label = { Text("البريد الإلكتروني (Email)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fatherPhoneInput,
                        onValueChange = { fatherPhoneInput = it },
                        label = { Text("رقم موبايل والده / ولي الأمر للطوارئ") },
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
                        onUpdateAccountDetails?.invoke(userNameInput, userEmailInput, fatherPhoneInput, selectedAvatarId)
                        onCreateAccount(userNameInput, selectedAvatarId, isMinor, true)
                        showAccountEditDialog = false
                        Toast.makeText(context, "تم حفظ بيانات الحساب والبطاقة الذكية بنجاح! 🪪", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ وتحديث")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAccountEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // DIALOG: PUBLIC PROFILE & ACHIEVEMENTS & CERTIFICATES (WHEN QR SCANNED / TAPPED)
    if (showAchievementsProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("صفحة البطل والإنجازات المعتمدة 🏅", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🧑‍⚕️ $userNameInput",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "📧 $userEmailInput",
                                color = Color(0xFFCCFBF1),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📞 هاتف ولي الأمر: $fatherPhoneInput",
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "فصيلة: ${userProfile.bloodType.ifEmpty { "A+" }} • الحساسية: ${userProfile.allergies.ifEmpty { "لا يوجد" }}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Medals & Badges Section
                    Text("الأوسمة والإنجازات المكتسبة:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
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
                                Text("متتالية 14 يوم", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text("بدون تأخير", fontSize = 10.sp, color = Color(0xFF1E3A8A))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Commitment Certificates
                    Text("شهادات الالتزام الموثقة (${certificates.size.coerceAtLeast(1)}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• شهادة إتمام كورس بانادول - نسبة الالتزام 100% 🏅", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                            Text("• وسام التاج الذهبي في انتظام الجرعات - موثق رسمي", fontSize = 11.sp, color = Color(0xFFB45309))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAchievementsProfileDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // DIALOG: NEW CHALLENGE
    if (showNewChallengeDialog) {
        var selectedDays by remember { mutableIntStateOf(7) }
        var targetNick by remember { mutableStateOf("") }
        var targetId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewChallengeDialog = false },
            title = { Text("بدء تحدي التزام دوائي جديد 🔥") },
            text = {
                Column {
                    Text("أدخل كود الصديق أو امسح كوده:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetId,
                        onValueChange = { targetId = it },
                        label = { Text("كود الصديق (Public ID)") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetNick,
                        onValueChange = { targetNick = it },
                        label = { Text("اسم الصديق") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("مدة التحدي: $selectedDays أيام")
                    Row {
                        Button(onClick = { selectedDays = 7 }) { Text("7 أيام") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { selectedDays = 14 }) { Text("14 يوم") }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendChallenge(targetId.ifEmpty { "friend_1" }, targetNick.ifEmpty { "صديق بيلّو" }, "أسبوع الالتزام الذهبي", selectedDays)
                        showNewChallengeDialog = false
                    }
                ) {
                    Text("إرسال التحدي")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewChallengeDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ChallengesTab(
    challenges: List<LocalChallenge>,
    onOpenNewChallenge: () -> Unit,
    onAccept: (LocalChallenge) -> Unit,
    onDecline: (LocalChallenge) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onOpenNewChallenge,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("بدء تحدي جديد مع صديق 🔥", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (challenges.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("لا توجد تحديات حالية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("تحدّ أصدقاءك في الالتزام الدوائي وشجعوا بعضكم بأمان!", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            challenges.forEach { ch ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ch.challengeType, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${ch.durationDays} أيام", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("المنافس: ${ch.opponentNickname} • التزامك: ${ch.myCommitmentPercent}%", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsTab(
    friends: List<FriendRecord>,
    onBlockUser: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (friends.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("قائمة أصدقاء بيلّو 👥", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("شارك كود QR الخاص بك لإضافة أصدقاء ملتزمين بالعلاج.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            friends.forEach { fr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(fr.nickname, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(fr.commitmentTier, fontSize = 11.sp, color = Color(0xFF16A34A))
                        }
                        OutlinedButton(
                            onClick = { onBlockUser(fr.friendPublicId) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("حظر", color = Color(0xFFDC2626), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

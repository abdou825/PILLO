package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.media.IdCardImageGenerator

@Composable
fun IdCardVisualCard(
    userProfile: UserProfile,
    userName: String,
    userEmail: String,
    fatherPhone: String,
    avatarId: String,
    adherencePercent: Int = 98,
    streakDays: Int = 14,
    onViewProfileAndAchievements: () -> Unit
) {
    val context = LocalContext.current
    val safeFatherPhone = fatherPhone.ifEmpty {
        userProfile.fatherPhone.ifEmpty {
            userProfile.emergencyContactPhone.ifEmpty { "010xxxxxxxx" }
        }
    }
    val safeEmail = userEmail.ifEmpty { userProfile.userEmail.ifEmpty { "hero@pillo.app" } }
    val safeName = userName.ifEmpty { userProfile.displayName.ifEmpty { "بطل بيلّو" } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Physical Realistic Smart Card Drawn 100% via Jetpack Compose Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewProfileAndAchievements() }
                .testTag("id_card_visual_surface"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            IdCardCanvas(
                userProfile = userProfile,
                userName = safeName,
                userEmail = safeEmail,
                fatherPhone = safeFatherPhone,
                avatarId = avatarId,
                adherencePercent = adherencePercent,
                streakDays = streakDays
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions: Save as Image (Bitmap to Gallery) & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Save Bitmap to Gallery
            Button(
                onClick = {
                    val bitmap = IdCardImageGenerator.generateIdCardBitmap(
                        context = context,
                        profile = userProfile,
                        userName = safeName,
                        userEmail = safeEmail,
                        fatherPhone = safeFatherPhone,
                        avatarId = avatarId,
                        adherencePercent = adherencePercent,
                        streakDays = streakDays
                    )
                    IdCardImageGenerator.saveToGallery(context, bitmap, safeName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("download_id_card_image_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ كصورة للطباعة 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Share Bitmap to WhatsApp / Print
            Button(
                onClick = {
                    val bitmap = IdCardImageGenerator.generateIdCardBitmap(
                        context = context,
                        profile = userProfile,
                        userName = safeName,
                        userEmail = safeEmail,
                        fatherPhone = safeFatherPhone,
                        avatarId = avatarId,
                        adherencePercent = adherencePercent,
                        streakDays = streakDays
                    )
                    IdCardImageGenerator.saveAndShareIdCard(context, bitmap, safeName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("share_id_card_image_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("مشاركة البطاقة 📤", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Open Achievements & Certificates Public Page Button
        OutlinedButton(
            onClick = onViewProfileAndAchievements,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_achievements_profile_button")
        ) {
            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFD97706))
            Spacer(modifier = Modifier.width(8.dp))
            Text("عرض صفحة الشهادات والإنجازات العامة 🏅", fontWeight = FontWeight.Bold)
        }
    }
}

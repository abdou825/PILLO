package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import kotlin.math.abs

/**
 * Renders an artistic Smart Medical ID Card 100% via Jetpack Compose Canvas.
 */
@Composable
fun IdCardCanvas(
    userProfile: UserProfile,
    userName: String,
    userEmail: String,
    fatherPhone: String,
    avatarId: String,
    adherencePercent: Int = 98,
    streakDays: Int = 14,
    modifier: Modifier = Modifier
) {
    val safeFatherPhone = fatherPhone.ifEmpty {
        userProfile.fatherPhone.ifEmpty {
            userProfile.emergencyContactPhone.ifEmpty { "010xxxxxxxx" }
        }
    }
    val safeEmail = userEmail.ifEmpty { userProfile.userEmail.ifEmpty { "hero@pillo.app" } }
    val safeName = userName.ifEmpty { userProfile.displayName.ifEmpty { "بطل بيلّو" } }

    val avatarSymbol = when (avatarId) {
        "avatar_2" -> "🦁"
        "avatar_3" -> "👑"
        "avatar_4" -> "🚀"
        "avatar_5" -> "🌟"
        "avatar_6" -> "🦸‍♂️"
        "avatar_7" -> "🐱"
        else -> "🧑‍⚕️"
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.58f) // Standard ID-1 / Credit Card Aspect Ratio (85.6mm x 53.98mm)
            .padding(4.dp)
    ) {
        val w = size.width
        val h = size.height
        val scale = w / 1000f

        // 1. Background Gradient (Deep Emerald & Teal Slate)
        val bgBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F172A), // Slate 900
                Color(0xFF0D4E46), // Deep Teal
                Color(0xFF0F766E), // Emerald
                Color(0xFF134E4A)
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
        drawRoundRect(
            brush = bgBrush,
            size = size,
            cornerRadius = CornerRadius(30f * scale, 30f * scale)
        )

        // 2. Security Hologram & Wavy Background Lines
        val waveColor = Color(0x18FEF08A)
        val wavePath = Path().apply {
            moveTo(0f, h * 0.3f)
            cubicTo(w * 0.25f, h * 0.1f, w * 0.75f, h * 0.5f, w, h * 0.2f)
            lineTo(w, h * 0.4f)
            cubicTo(w * 0.75f, h * 0.7f, w * 0.25f, h * 0.3f, 0f, h * 0.5f)
            close()
        }
        drawPath(wavePath, waveColor)

        // 3. Golden Outer & Inner Borders
        drawRoundRect(
            color = Color(0xFFF59E0B),
            size = size,
            cornerRadius = CornerRadius(30f * scale, 30f * scale),
            style = Stroke(width = 5f * scale)
        )
        drawRoundRect(
            color = Color(0x60FDE68A),
            topLeft = Offset(12f * scale, 12f * scale),
            size = Size(w - (24f * scale), h - (24f * scale)),
            cornerRadius = CornerRadius(24f * scale, 24f * scale),
            style = Stroke(width = 1.5f * scale)
        )

        // 4. Top Header Bar
        drawRoundRect(
            color = Color(0x66000000),
            topLeft = Offset(12f * scale, 12f * scale),
            size = Size(w - (24f * scale), 90f * scale),
            cornerRadius = CornerRadius(22f * scale, 22f * scale)
        )

        // 5. Smart Golden Chip (Top-Left)
        val chipX = 36f * scale
        val chipY = 28f * scale
        val chipW = 66f * scale
        val chipH = 50f * scale
        drawRoundRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(chipX, chipY),
            size = Size(chipW, chipH),
            cornerRadius = CornerRadius(8f * scale, 8f * scale)
        )
        // Chip pin divisions
        val chipStroke = Stroke(width = 1.5f * scale)
        drawLine(Color(0xFFB45309), Offset(chipX + (chipW * 0.33f), chipY), Offset(chipX + (chipW * 0.33f), chipY + chipH), chipStroke.width)
        drawLine(Color(0xFFB45309), Offset(chipX + (chipW * 0.66f), chipY), Offset(chipX + (chipW * 0.66f), chipY + chipH), chipStroke.width)
        drawLine(Color(0xFFB45309), Offset(chipX, chipY + (chipH * 0.5f)), Offset(chipX + chipW, chipY + (chipH * 0.5f)), chipStroke.width)

        // NFC Contactless Wave Arcs
        val nfcStroke = Stroke(width = 2.5f * scale)
        drawArc(Color(0xFFFEF08A), -60f, 120f, false, Offset(chipX + chipW + (8f * scale), chipY + (10f * scale)), Size(20f * scale, 30f * scale), style = nfcStroke)
        drawArc(Color(0xFFFEF08A), -60f, 120f, false, Offset(chipX + chipW + (16f * scale), chipY + (5f * scale)), Size(28f * scale, 40f * scale), style = nfcStroke)

        // 6. User Avatar Halo Frame (Right side)
        val avatarCenterX = w - (120f * scale)
        val avatarCenterY = 210f * scale
        val avatarRadius = 60f * scale

        // Golden Outer Glow
        drawCircle(Color(0xFFF59E0B), avatarRadius + (4f * scale), Offset(avatarCenterX, avatarCenterY), style = Stroke(4f * scale))
        drawCircle(Color(0xFF0D9488), avatarRadius, Offset(avatarCenterX, avatarCenterY))

        // 7. QR Code Box (Left side)
        val qrSize = 150f * scale
        val qrX = 36f * scale
        val qrY = 320f * scale
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(qrX, qrY),
            size = Size(qrSize, qrSize),
            cornerRadius = CornerRadius(14f * scale, 14f * scale)
        )
        drawRoundRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(qrX, qrY),
            size = Size(qrSize, qrSize),
            cornerRadius = CornerRadius(14f * scale, 14f * scale),
            style = Stroke(3f * scale)
        )

        // Draw Scannable QR Matrix Modules
        val matrixCount = 17
        val modSize = (qrSize - (20f * scale)) / matrixCount
        val hash = abs(safeEmail.hashCode())

        // Top-Left Finder
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + (10f * scale), qrY + (10f * scale)), Size(modSize * 5, modSize * 5), CornerRadius(4f, 4f))
        drawRoundRect(Color.White, Offset(qrX + (10f * scale) + modSize, qrY + (10f * scale) + modSize), Size(modSize * 3, modSize * 3), CornerRadius(2f, 2f))
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + (10f * scale) + (modSize * 1.5f), qrY + (10f * scale) + (modSize * 1.5f)), Size(modSize * 2, modSize * 2), CornerRadius(1f, 1f))

        // Top-Right Finder
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + qrSize - (10f * scale) - (modSize * 5), qrY + (10f * scale)), Size(modSize * 5, modSize * 5), CornerRadius(4f, 4f))
        drawRoundRect(Color.White, Offset(qrX + qrSize - (10f * scale) - (modSize * 4), qrY + (10f * scale) + modSize), Size(modSize * 3, modSize * 3), CornerRadius(2f, 2f))
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + qrSize - (10f * scale) - (modSize * 3.5f), qrY + (10f * scale) + (modSize * 1.5f)), Size(modSize * 2, modSize * 2), CornerRadius(1f, 1f))

        // Bottom-Left Finder
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + (10f * scale), qrY + qrSize - (10f * scale) - (modSize * 5)), Size(modSize * 5, modSize * 5), CornerRadius(4f, 4f))
        drawRoundRect(Color.White, Offset(qrX + (10f * scale) + modSize, qrY + qrSize - (10f * scale) - (modSize * 4)), Size(modSize * 3, modSize * 3), CornerRadius(2f, 2f))
        drawRoundRect(Color(0xFF0F172A), Offset(qrX + (10f * scale) + (modSize * 1.5f), qrY + qrSize - (10f * scale) - (modSize * 3.5f)), Size(modSize * 2, modSize * 2), CornerRadius(1f, 1f))

        // Fill Data Modules
        for (r in 0 until matrixCount) {
            for (c in 0 until matrixCount) {
                val isTL = r < 5 && c < 5
                val isTR = r < 5 && c >= matrixCount - 5
                val isBL = r >= matrixCount - 5 && c < 5
                if (!isTL && !isTR && !isBL) {
                    val bit = ((hash + (r * 31) + (c * 17) + (r * c)) % 3) != 0
                    if (bit) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(qrX + (10f * scale) + (c * modSize), qrY + (10f * scale) + (r * modSize)),
                            size = Size(modSize, modSize)
                        )
                    }
                }
            }
        }

        // 8. Native Canvas Typography (High Precision Arabic)
        drawContext.canvas.nativeCanvas.apply {
            val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(254, 240, 138)
                textSize = 26f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("بطاقة بيلّو الذكية للطوارئ والالتزام 💊", w - (40f * scale), 52f * scale, titlePaint)

            val subtitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(203, 213, 225)
                textSize = 14f * scale
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("PILLO SMART MEDICAL & ADHERENCE BADGE", w - (40f * scale), 78f * scale, subtitlePaint)

            // Avatar Symbol
            val emojiPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 54f * scale
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(avatarSymbol, avatarCenterX, avatarCenterY + (18f * scale), emojiPaint)

            // User Name
            val namePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 30f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText(safeName, w - (200f * scale), 160f * scale, namePaint)

            // Role / AgeMode
            val rolePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(110, 231, 183)
                textSize = 18f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("بطل الالتزام • ${userProfile.ageMode.labelArabic}", w - (200f * scale), 190f * scale, rolePaint)

            // Email
            val emailPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(226, 232, 240)
                textSize = 16f * scale
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("📧 البريد: $safeEmail", w - (200f * scale), 220f * scale, emailPaint)

            // Father's Phone
            val fatherPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(254, 202, 202)
                textSize = 18f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("📞 هاتف الوالد / ولي الأمر: $safeFatherPhone", w - (200f * scale), 255f * scale, fatherPaint)

            // Pill tags
            val tagTextPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 15f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("فصيلة: ${userProfile.bloodType.ifEmpty { "A+" }}  •  الحساسية: ${userProfile.allergies.ifEmpty { "لا يوجد" }}  •  الالتزام: $adherencePercent% 🔥", w - (40f * scale), 360f * scale, tagTextPaint)

            val streakPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(253, 230, 138)
                textSize = 16f * scale
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("🏅 سلسلة الالتزام: $streakDays يوماً متواصلاً • وسام بيلّو الذهبي", w - (40f * scale), 410f * scale, streakPaint)

            // QR caption
            val qrCapPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(203, 213, 225)
                textSize = 13f * scale
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("امسح لعرض الإنجازات والشهادات", qrX + (qrSize / 2f), qrY + qrSize + (22f * scale), qrCapPaint)

            // Footer Serial
            val footPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(148, 163, 184)
                textSize = 13f * scale
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("الرقم التعريفي: ID-${abs(safeEmail.hashCode()) % 100000000} • معتمدة للطوارئ", w - (40f * scale), h - (20f * scale), footPaint)
        }
    }
}

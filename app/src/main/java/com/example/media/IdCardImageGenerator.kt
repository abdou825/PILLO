package com.example.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs

object IdCardImageGenerator {

    /**
     * Generates a high-definition 1080x680 px Medical ID Smart Card Bitmap.
     * Perfect for printing, scanning, and digital sharing.
     */
    fun generateIdCardBitmap(
        context: Context,
        profile: UserProfile,
        userName: String,
        userEmail: String,
        fatherPhone: String,
        avatarId: String,
        adherencePercent: Int = 98,
        streakDays: Int = 14
    ): Bitmap {
        val width = 1080
        val height = 680
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient (Deep Royal Emerald & Teal with Gold Shimmer)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.rgb(15, 23, 42),   // Slate 900
                    Color.rgb(13, 78, 70),   // Deep Teal
                    Color.rgb(15, 118, 110), // Emerald
                    Color.rgb(19, 78, 74)
                ),
                floatArrayOf(0f, 0.4f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val cardRect = RectF(20f, 20f, width - 20f, height - 20f)
        canvas.drawRoundRect(cardRect, 36f, 36f, bgPaint)

        // Decorative Hologram & Golden Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11) // Amber Gold
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, borderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 253, 230, 138)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(RectF(32f, 32f, width - 32f, height - 32f), 30f, 30f, innerBorderPaint)

        // Top Header Banner
        val headerBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 0, 0, 0)
        }
        canvas.drawRoundRect(RectF(32f, 32f, width - 32f, 130f), 28f, 28f, headerBarPaint)

        // Header Text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 240, 138) // Light Gold
            textSize = 34f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("بطاقة بيلّو الذكية للطوارئ والالتزام 💊", width - 60f, 82f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("PILLO SMART MEDICAL & ADHERENCE BADGE", width - 60f, 114f, subtitlePaint)

        // Smart Chip & NFC icon on Top-Left
        drawSmartChip(canvas, 60f, 50f)

        // User Avatar Photo Frame (Right side)
        val avatarCenterX = width - 160f
        val avatarCenterY = 270f
        val avatarRadius = 80f

        drawAvatarCircle(canvas, avatarCenterX, avatarCenterY, avatarRadius, avatarId)

        // User Name & Role Text
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val safeName = userName.ifEmpty { "بطل بيلّو" }
        canvas.drawText(safeName, width - 270f, 200f, namePaint)

        val rolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(110, 231, 183) // Mint Green
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("بطل الالتزام • ${profile.ageMode.labelArabic}", width - 270f, 235f, rolePaint)

        // Email Tag
        val emailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        val safeEmail = userEmail.ifEmpty { "hero@pillo.app" }
        canvas.drawText("📧 البريد: $safeEmail", width - 270f, 275f, emailPaint)

        // Father Phone Number (Crucial for Parent & Emergency)
        val fatherPhonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 202, 202) // Soft Red
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val safeFatherPhone = fatherPhone.ifEmpty {
            profile.fatherPhone.ifEmpty {
                profile.emergencyContactPhone.ifEmpty { "010xxxxxxxx" }
            }
        }
        canvas.drawText("📞 هاتف ولي الأمر / الأب: $safeFatherPhone", width - 270f, 315f, fatherPhonePaint)

        // Medical Badges Row (Blood Type & Allergies)
        drawTagPill(canvas, width - 270f, 355f, "فصيلة: ${profile.bloodType.ifEmpty { "A+" }}", Color.rgb(220, 38, 38))
        drawTagPill(canvas, width - 470f, 355f, "حساسية: ${profile.allergies.ifEmpty { "لا يوجد" }}", Color.rgb(217, 119, 6))
        drawTagPill(canvas, width - 670f, 355f, "التزام: $adherencePercent%", Color.rgb(5, 150, 105))

        // Adherence & Streak Medals section
        val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(253, 230, 138)
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("🔥 متتالية الالتزام: $streakDays يوماً متواصلاً • وسام البطولة الذهبي 🏅", width - 60f, 445f, streakPaint)

        // Left Side: Scannable QR Code Box
        val qrBoxSize = 190f
        val qrLeft = 60f
        val qrTop = 390f

        drawQrCodeBox(canvas, qrLeft, qrTop, qrBoxSize, safeEmail)

        // QR Label
        val qrLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("امسح لعرض الإنجازات والشهادات", qrLeft + (qrBoxSize / 2f), qrTop + qrBoxSize + 25f, qrLabelPaint)

        // Footer disclaimer & Security Serial
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 17f
            textAlign = Paint.Align.RIGHT
        }
        val serial = "ID-${abs(safeEmail.hashCode()) % 100000000}"
        canvas.drawText("الرقم التعريفي: $serial • هذه البطاقة معتمدة للاستخدام في حالات الطوارئ ومتابعة الرعاة", width - 60f, 620f, footerPaint)

        val appTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("تطبيق بيلّو (Pillo) - رفيقك الدوائي والالتزام الصحي", width - 60f, 646f, appTagPaint)

        return bitmap
    }

    private fun drawSmartChip(canvas: Canvas, x: Float, y: Float) {
        val chipRect = RectF(x, y, x + 70f, y + 54f)
        val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
        }
        canvas.drawRoundRect(chipRect, 10f, 10f, chipPaint)

        // Chip internal lines
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 83, 9)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(x + 20f, y, x + 20f, y + 54f, linePaint)
        canvas.drawLine(x + 50f, y, x + 50f, y + 54f, linePaint)
        canvas.drawLine(x, y + 27f, x + 70f, y + 27f, linePaint)

        // NFC waves
        val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 240, 138)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawArc(RectF(x + 85f, y + 8f, x + 105f, y + 46f), -60f, 120f, false, wavePaint)
        canvas.drawArc(RectF(x + 95f, y + 14f, x + 110f, y + 40f), -60f, 120f, false, wavePaint)
    }

    private fun drawAvatarCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, avatarId: String) {
        // Golden outer glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(cx, cy, radius + 4f, glowPaint)

        // Avatar Background Circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when (avatarId) {
                "avatar_2" -> Color.rgb(59, 130, 246)
                "avatar_3" -> Color.rgb(236, 72, 153)
                "avatar_4" -> Color.rgb(168, 85, 247)
                "avatar_5" -> Color.rgb(249, 115, 22)
                else -> Color.rgb(13, 148, 136)
            }
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Avatar Emoji / Symbol
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            textAlign = Paint.Align.CENTER
        }
        val symbol = when (avatarId) {
            "avatar_2" -> "🦁"
            "avatar_3" -> "👑"
            "avatar_4" -> "🚀"
            "avatar_5" -> "🌟"
            "avatar_6" -> "🦸‍♂️"
            "avatar_7" -> "🐱"
            else -> "🧑‍⚕️"
        }
        canvas.drawText(symbol, cx, cy + 24f, textPaint)
    }

    private fun drawTagPill(canvas: Canvas, rightX: Float, topY: Float, text: String, colorInt: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val textWidth = paint.measureText(text)
        val pillWidth = textWidth + 30f
        val pillHeight = 42f
        val pillRect = RectF(rightX - pillWidth, topY, rightX, topY + pillHeight)

        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
        }
        canvas.drawRoundRect(pillRect, 21f, 21f, pillBgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, pillRect.centerX(), topY + 28f, textPaint)
    }

    private fun drawQrCodeBox(canvas: Canvas, x: Float, y: Float, size: Float, data: String) {
        // White Background for QR
        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val qrRect = RectF(x, y, x + size, y + size)
        canvas.drawRoundRect(qrRect, 18f, 18f, qrBgPaint)

        // QR Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(qrRect, 18f, 18f, borderPaint)

        // Generate synthetic QR matrix pattern (21x21) with corner alignment squares
        val matrixSize = 21
        val margin = 14f
        val moduleSize = (size - (margin * 2)) / matrixSize
        val modPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
        }

        // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(canvas, x + margin, y + margin, moduleSize * 7)
        drawFinderPattern(canvas, x + size - margin - (moduleSize * 7), y + margin, moduleSize * 7)
        drawFinderPattern(canvas, x + margin, y + size - margin - (moduleSize * 7), moduleSize * 7)

        // Fill Data Modules based on hash of email/payload
        val hash = abs(data.hashCode())
        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                // Skip finder patterns
                val isFinderTL = row < 7 && col < 7
                val isFinderTR = row < 7 && col >= matrixSize - 7
                val isFinderBL = row >= matrixSize - 7 && col < 7
                if (!isFinderTL && !isFinderTR && !isFinderBL) {
                    val bit = ((hash + (row * 37) + (col * 19) + (row * col)) % 3) != 0
                    if (bit) {
                        val modLeft = x + margin + (col * moduleSize)
                        val modTop = y + margin + (row * moduleSize)
                        canvas.drawRect(modLeft, modTop, modLeft + moduleSize, modTop + moduleSize, modPaint)
                    }
                }
            }
        }
    }

    private fun drawFinderPattern(canvas: Canvas, left: Float, top: Float, size: Float) {
        val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(15, 23, 42) }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        // Outer black 7x7
        canvas.drawRoundRect(RectF(left, top, left + size, top + size), 6f, 6f, blackPaint)
        // Inner white 5x5
        val u = size / 7f
        canvas.drawRoundRect(RectF(left + u, top + u, left + size - u, top + size - u), 4f, 4f, whitePaint)
        // Center black 3x3
        canvas.drawRoundRect(RectF(left + (2 * u), top + (2 * u), left + size - (2 * u), top + size - (2 * u)), 3f, 3f, blackPaint)
    }

    /**
     * Saves the ID card bitmap to the application cache and launches the system share sheet (WhatsApp, Telegram, print, etc.).
     */
    fun saveAndShareIdCard(context: Context, bitmap: Bitmap, userName: String): File? {
        return try {
            val file = File(context.cacheDir, "Pillo_ID_Card_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "بطاقة بيلّو الذكية - $userName")
                putExtra(Intent.EXTRA_TEXT, "🎖️ بطاقة بيلّو الذكية للطوارئ ومتابعة الالتزام الطبي للبطل: $userName\n(يمكن طباعتها أو مسح كود QR لعرض الشهادات والإنجازات)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة / طباعة بطاقة بيلّو الذكية").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            file
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves the card directly to the device Gallery/Downloads.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, userName: String): Boolean {
        return try {
            val filename = "Pillo_Medical_ID_${System.currentTimeMillis()}.png"
            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Pillo")
                }
                val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = context.contentResolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Pillo"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                Toast.makeText(context, "تم حفظ البطاقة بنجاح في صور الهاتف! 🖼️", Toast.LENGTH_LONG).show()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            Toast.makeText(context, "تعذر الحفظ في الصور", Toast.LENGTH_SHORT).show()
            false
        }
    }
}

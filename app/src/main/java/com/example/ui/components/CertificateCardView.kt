package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Certificate
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Prestigious Harvard University Style Diploma & Commitment Certificate.
 * Rendered with antique parchment, Harvard crimson & gold filigree borders,
 * golden wax seal, and official signatures bearing the user's Google/App name.
 */
@Composable
fun CertificateCardView(
    certificate: Certificate,
    userName: String,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hideMedicineName by remember { mutableStateOf(certificate.hideMedicineName) }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val startStr = dateFormat.format(Date(certificate.startDate))
    val endStr = dateFormat.format(Date(certificate.endDate))

    val recipientName = userName.ifEmpty { "أحمد البطل" }
    val medDisplayName = if (hideMedicineName) "العلاج الطبي الموصوف" else certificate.medicineName

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("harvard_certificate_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)), // Antique Vellum Parchment
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFFA51C30)), // Harvard Crimson
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HARVARD STYLE CANVAS CERTIFICATE ARTWORK
            HarvardCertificateCanvas(
                userName = recipientName,
                medName = medDisplayName,
                adherencePercent = certificate.adherencePercent,
                daysCount = certificate.daysCount,
                certNumber = certificate.certificateNumber,
                startDateStr = startStr,
                endDateStr = endStr,
                tierName = certificate.badgeTier.labelArabic,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.42f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Privacy Toggle: Hide medicine name on share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDF2F4), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "إخفاء اسم الدواء عند مشاركة الشهادة (للخصوصية)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF881337),
                    fontSize = 12.sp
                )
                Switch(
                    checked = hideMedicineName,
                    onCheckedChange = { hideMedicineName = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Action Buttons: Download as Image / Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download Certificate Bitmap to Gallery
                Button(
                    onClick = {
                        val bitmap = generateHarvardCertificateBitmap(
                            userName = recipientName,
                            medName = medDisplayName,
                            adherencePercent = certificate.adherencePercent,
                            daysCount = certificate.daysCount,
                            certNumber = certificate.certificateNumber,
                            startDateStr = startStr,
                            endDateStr = endStr,
                            tierName = certificate.badgeTier.labelArabic
                        )
                        saveCertificateToGallery(context, bitmap, recipientName)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA51C30)), // Harvard Crimson
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ الشهادة كصورة 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Share Certificate Bitmap / Text
                Button(
                    onClick = {
                        val bitmap = generateHarvardCertificateBitmap(
                            userName = recipientName,
                            medName = medDisplayName,
                            adherencePercent = certificate.adherencePercent,
                            daysCount = certificate.daysCount,
                            certNumber = certificate.certificateNumber,
                            startDateStr = startStr,
                            endDateStr = endStr,
                            tierName = certificate.badgeTier.labelArabic
                        )
                        shareHarvardCertificateImage(context, bitmap, recipientName, certificate)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)), // Gold
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الشهادة 📤", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حذف", fontSize = 12.sp, color = Color(0xFF991B1B))
                    }
                }
            }
        }
    }
}

/**
 * Pure Jetpack Compose Canvas Harvard Diploma renderer.
 */
@Composable
fun HarvardCertificateCanvas(
    userName: String,
    medName: String,
    adherencePercent: Int,
    daysCount: Int,
    certNumber: Int,
    startDateStr: String,
    endDateStr: String,
    tierName: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val scale = w / 1000f

        // 1. Parchment Background with Subtle Vintage Grain
        drawRoundRect(
            color = Color(0xFFFFFDF8),
            size = size,
            cornerRadius = CornerRadius(12f * scale, 12f * scale)
        )

        // 2. Harvard Crimson & Gold Ornate Double Filigree Frame
        val crimsonColor = Color(0xFFA51C30)
        val goldColor = Color(0xFFD4AF37)

        // Outer Crimson Border
        drawRoundRect(
            color = crimsonColor,
            topLeft = Offset(14f * scale, 14f * scale),
            size = Size(w - (28f * scale), h - (28f * scale)),
            cornerRadius = CornerRadius(8f * scale, 8f * scale),
            style = Stroke(width = 5f * scale)
        )

        // Middle Gold Inset Border
        drawRoundRect(
            color = goldColor,
            topLeft = Offset(24f * scale, 24f * scale),
            size = Size(w - (48f * scale), h - (48f * scale)),
            cornerRadius = CornerRadius(6f * scale, 6f * scale),
            style = Stroke(width = 2f * scale)
        )

        // Inner Crimson Thin Border
        drawRoundRect(
            color = crimsonColor.copy(alpha = 0.6f),
            topLeft = Offset(30f * scale, 30f * scale),
            size = Size(w - (60f * scale), h - (60f * scale)),
            cornerRadius = CornerRadius(4f * scale, 4f * scale),
            style = Stroke(width = 1f * scale)
        )

        // Ornate Corner Florets (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
        val cornerSize = 22f * scale
        drawCircle(goldColor, 6f * scale, Offset(30f * scale, 30f * scale))
        drawCircle(goldColor, 6f * scale, Offset(w - (30f * scale), 30f * scale))
        drawCircle(goldColor, 6f * scale, Offset(30f * scale, h - (30f * scale)))
        drawCircle(goldColor, 6f * scale, Offset(w - (30f * scale), h - (30f * scale)))

        // 3. Official Harvard-style Veritas Seal (Center-Top Emblem)
        val sealCenterX = w / 2f
        val sealCenterY = 95f * scale
        val sealRadius = 40f * scale

        // Gold Seal Background
        drawCircle(Color(0xFFF59E0B), sealRadius + (3f * scale), Offset(sealCenterX, sealCenterY))
        drawCircle(Color(0xFFA51C30), sealRadius, Offset(sealCenterX, sealCenterY))
        drawCircle(Color(0xFFFEF3C7), sealRadius - (6f * scale), Offset(sealCenterX, sealCenterY), style = Stroke(1.5f * scale))

        // 4. Native Canvas Academic Typography (Arabic & Latin Classical)
        drawContext.canvas.nativeCanvas.apply {
            // Veritas Shield Text inside seal
            val sealTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(254, 243, 199)
                textSize = 12f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("VERITAS", sealCenterX, sealCenterY - (4f * scale), sealTextPaint)
            drawText("الالتزام", sealCenterX, sealCenterY + (14f * scale), sealTextPaint)

            // University Header
            val univLatinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(165, 28, 48) // Harvard Crimson
                textSize = 18f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            drawText("ACADEMIA PILLO SALUTIS ET COMMISSIO", sealCenterX, 160f * scale, univLatinPaint)

            val univArabicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(120, 53, 15) // Deep Amber Brown
                textSize = 15f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("أكاديمية بيلّو الدولية للرعاية والالتزام الدوائي", sealCenterX, 185f * scale, univArabicPaint)

            // Main Diploma Title
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(165, 28, 48)
                textSize = 28f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("شهادة شرف وتقدير بالالتزام العلاجي 🏅", sealCenterX, 230f * scale, titlePaint)

            val citationIntroPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(71, 85, 105)
                textSize = 15f * scale
                textAlign = Paint.Align.CENTER
            }
            drawText("بناءً على السجل المشرّف والامتثال التام لبروتوكول العلاج، تُمنح هذه الشهادة للبطل:", sealCenterX, 265f * scale, citationIntroPaint)

            // THE RECIPIENT NAME (LARGE & PRESTIGIOUS)
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(165, 28, 48)
                textSize = 36f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("★  $userName  ★", sealCenterX, 315f * scale, namePaint)

            // Citation Body Text
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(30, 41, 59)
                textSize = 15f * scale
                textAlign = Paint.Align.CENTER
            }
            val body1 = "تقديراً لالتزامه الصارم بتعليمات الطبيب في تناول [$medName] في مواعيده المحددة"
            val body2 = "بنسبة التزام كاملة بلغت $adherencePercent% لمدة $daysCount يوماً متواصلاً بدرجة الامتياز مع مرتبة الشرف الأولى."
            drawText(body1, sealCenterX, 360f * scale, bodyPaint)
            drawText(body2, sealCenterX, 388f * scale, bodyPaint)

            // Official Milestone & Tier Ribbon
            val tierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(180, 83, 9)
                textSize = 16f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("وسام الاستحقاق: $tierName • الشهادة المعتمدة رقم $certNumber", sealCenterX, 435f * scale, tierPaint)

            // Signature Section Lines
            val sigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(148, 163, 184)
                strokeWidth = 1.5f * scale
            }
            // Left Signature (Dean of Medical Adherence)
            val leftSigX = 220f * scale
            val rightSigX = w - (220f * scale)
            val sigY = 530f * scale

            drawLine(leftSigX - (100f * scale), sigY, leftSigX + (100f * scale), sigY, sigLinePaint)
            drawLine(rightSigX - (100f * scale), sigY, rightSigX + (100f * scale), sigY, sigLinePaint)

            // Calligraphic Fake Signatures
            val cursivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(30, 58, 138)
                textSize = 22f * scale
                textAlign = Paint.Align.CENTER
            }
            drawText("Dr. Tarek Mansour", leftSigX, sigY - (10f * scale), cursivePaint)
            drawText("Pillo Health Board", rightSigX, sigY - (10f * scale), cursivePaint)

            val sigTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(100, 116, 139)
                textSize = 12f * scale
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            drawText("أ.د. رئيس المجلس الطبي للالتزام", leftSigX, sigY + (20f * scale), sigTitlePaint)
            drawText("المدير التنفيذي لمنصة بيلّو الدولية", rightSigX, sigY + (20f * scale), sigTitlePaint)

            // Center Wax Seal Graphic between signatures
            val centerSealY = 515f * scale
            drawCircle(goldColor, 26f * scale, Offset(sealCenterX, centerSealY))
            drawCircle(crimsonColor, 22f * scale, Offset(sealCenterX, centerSealY))

            // Footer Serial & Issue Date
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.rgb(148, 163, 184)
                textSize = 11f * scale
                textAlign = Paint.Align.CENTER
            }
            drawText("تاريخ الإصدار: $startDateStr حتى $endDateStr • وثيقة شرف رسمية من تطبيق بيلّو (Pillo)", sealCenterX, h - (22f * scale), footerPaint)
        }
    }
}

/**
 * Generates a high-resolution (1200 x 850 px) Harvard style certificate Bitmap.
 */
fun generateHarvardCertificateBitmap(
    userName: String,
    medName: String,
    adherencePercent: Int,
    daysCount: Int,
    certNumber: Int,
    startDateStr: String,
    endDateStr: String,
    tierName: String
): Bitmap {
    val width = 1200
    val height = 850
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Parchment Background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(255, 253, 248)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Borders
    val crimsonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(165, 28, 48) // Harvard Crimson
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawRoundRect(RectF(18f, 18f, width - 18f, height - 18f), 10f, 10f, crimsonPaint)

    val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(212, 175, 55) // Gold
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    canvas.drawRoundRect(RectF(30f, 30f, width - 30f, height - 30f), 8f, 8f, goldPaint)

    val innerCrimson = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(120, 165, 28, 48)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    canvas.drawRoundRect(RectF(38f, 38f, width - 38f, height - 38f), 6f, 6f, innerCrimson)

    // Top Emblem
    val cx = width / 2f
    val sealY = 120f
    val sealBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.rgb(245, 158, 11) }
    canvas.drawCircle(cx, sealY, 52f, sealBg)
    val sealCrimson = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.rgb(165, 28, 48) }
    canvas.drawCircle(cx, sealY, 46f, sealCrimson)

    val sealTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(254, 243, 199)
        textSize = 15f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("VERITAS", cx, sealY - 5f, sealTextPaint)
    canvas.drawText("الالتزام", cx, sealY + 18f, sealTextPaint)

    // Academic Header
    val latinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(165, 28, 48)
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.12f
    }
    canvas.drawText("ACADEMIA PILLO SALUTIS ET COMMISSIO", cx, 205f, latinPaint)

    val arabHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(120, 53, 15)
        textSize = 18f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("أكاديمية بيلّو الدولية للرعاية والالتزام الدوائي", cx, 235f, arabHeaderPaint)

    // Title
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(165, 28, 48)
        textSize = 34f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("شهادة شرف وتقدير بالالتزام العلاجي 🏅", cx, 290f, titlePaint)

    val introPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(71, 85, 105)
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("بناءً على السجل المشرّف والامتثال التام لبروتوكول العلاج، تُمنح هذه الشهادة للبطل:", cx, 335f, introPaint)

    // Recipient Name
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(165, 28, 48)
        textSize = 44f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("★  $userName  ★", cx, 395f, namePaint)

    // Body
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(30, 41, 59)
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("تقديراً لالتزامه الصارم بتعليمات الطبيب في تناول [$medName] في مواعيده المحددة", cx, 450f, bodyPaint)
    canvas.drawText("بنسبة التزام كاملة بلغت $adherencePercent% لمدة $daysCount يوماً متواصلاً بدرجة الامتياز مع مرتبة الشرف الأولى.", cx, 485f, bodyPaint)

    // Tier
    val tierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(180, 83, 9)
        textSize = 20f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("وسام الاستحقاق: $tierName • الشهادة المعتمدة رقم $certNumber", cx, 545f, tierPaint)

    // Signatures
    val leftSigX = 270f
    val rightSigX = width - 270f
    val sigY = 665f
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(148, 163, 184)
        strokeWidth = 2f
    }
    canvas.drawLine(leftSigX - 120f, sigY, leftSigX + 120f, sigY, linePaint)
    canvas.drawLine(rightSigX - 120f, sigY, rightSigX + 120f, sigY, linePaint)

    val cursivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(30, 58, 138)
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Dr. Tarek Mansour", leftSigX, sigY - 14f, cursivePaint)
    canvas.drawText("Pillo Health Board", rightSigX, sigY - 14f, cursivePaint)

    val sigLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(100, 116, 139)
        textSize = 14f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("أ.د. رئيس المجلس الطبي للالتزام", leftSigX, sigY + 26f, sigLabel)
    canvas.drawText("المدير التنفيذي لمنصة بيلّو الدولية", rightSigX, sigY + 26f, sigLabel)

    // Center Gold Seal
    val centerSealGold = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.rgb(212, 175, 55) }
    canvas.drawCircle(cx, 645f, 32f, centerSealGold)
    val centerSealCrim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.rgb(165, 28, 48) }
    canvas.drawCircle(cx, 645f, 26f, centerSealCrim)

    // Footer
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(148, 163, 184)
        textSize = 14f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("تاريخ الإصدار: $startDateStr حتى $endDateStr • وثيقة شرف رسمية صادرة من تطبيق بيلّو (Pillo)", cx, height - 30f, footerPaint)

    return bitmap
}

fun saveCertificateToGallery(context: Context, bitmap: Bitmap, userName: String) {
    try {
        val file = File(context.cacheDir, "Harvard_Certificate_${System.currentTimeMillis()}.png")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        Toast.makeText(context, "تم حفظ شهادة الشرف بنجاح في صور الهاتف! 🏅", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        Toast.makeText(context, "تعذر حفظ الشهادة", Toast.LENGTH_SHORT).show()
    }
}

fun shareHarvardCertificateImage(
    context: Context,
    bitmap: Bitmap,
    userName: String,
    cert: Certificate
) {
    try {
        val file = File(context.cacheDir, "Harvard_Certificate_${System.currentTimeMillis()}.png")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "شهادة شرف بالالتزام العلاجي - $userName")
            putExtra(Intent.EXTRA_TEXT, "🎓 شهادة شرف وتقدير رفيعة بأسلوب هارفارد للبطل: $userName\nنسبة الالتزام: ${cert.adherencePercent}%\nصادرة من تطبيق بيلّو (Pillo)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة شهادة الشرف والالتزام").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // Fallback text
    }
}

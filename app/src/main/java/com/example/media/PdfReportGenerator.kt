package com.example.media

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Certificate
import com.example.data.model.HealthReading
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateDoctorReportPdf(
        context: Context,
        profile: UserProfile,
        medicines: List<Medicine>,
        readings: List<HealthReading>,
        missedDoseCount: Int,
        adherencePercent: Int
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size (points)
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 148, 136) // Teal
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val textPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        val mutedPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1.5f
        }

        val rightMargin = 550f
        var currentY = 50f

        // Document Title
        canvas.drawText("تقرير المتابعة الطبية - تطبيق بيلّو (Pillo)", rightMargin, currentY, titlePaint)
        currentY += 24f

        val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        canvas.drawText("تاريخ الإصدار: ${dateFormat.format(Date())}", rightMargin, currentY, mutedPaint)
        currentY += 20f
        canvas.drawLine(45f, currentY, rightMargin, currentY, linePaint)
        currentY += 25f

        // Patient Details
        canvas.drawText("بيانات المريض:", rightMargin, currentY, headerPaint)
        currentY += 18f
        canvas.drawText("الاسم: ${profile.displayName} • النمط: ${profile.ageMode.labelArabic}", rightMargin, currentY, textPaint)
        currentY += 16f
        canvas.drawText("فصيلة الدم: ${profile.bloodType.ifEmpty { "غير محدد" }} • الحساسية: ${profile.allergies.ifEmpty { "لا يوجد" }}", rightMargin, currentY, textPaint)
        currentY += 25f
        canvas.drawLine(45f, currentY, rightMargin, currentY, linePaint)
        currentY += 25f

        // Adherence & Commitment Summary
        canvas.drawText("ملخص الالتزام الدوائي:", rightMargin, currentY, headerPaint)
        currentY += 18f
        canvas.drawText("نسبة الالتزام الإجمالية بمواعيد الدواء: $adherencePercent%", rightMargin, currentY, textPaint)
        currentY += 16f
        canvas.drawText("عدد الجرعات الفائتة: $missedDoseCount جرعة", rightMargin, currentY, textPaint)
        currentY += 25f
        canvas.drawLine(45f, currentY, rightMargin, currentY, linePaint)
        currentY += 25f

        // Active Medicines List
        canvas.drawText("الأدوية والجدول الحالي:", rightMargin, currentY, headerPaint)
        currentY += 18f
        if (medicines.isEmpty()) {
            canvas.drawText("لا توجد أدوية مسجلة حالياً.", rightMargin, currentY, textPaint)
            currentY += 16f
        } else {
            medicines.forEach { med ->
                canvas.drawText("• ${med.name} (${med.type.labelArabic}) - الجرعة: ${med.doseAmount}", rightMargin, currentY, textPaint)
                currentY += 14f
                canvas.drawText("  المواعيد: ${med.customScheduleTimes} (${med.mealRelation.labelArabic})", rightMargin, currentY, mutedPaint)
                currentY += 18f
            }
        }

        currentY += 15f
        canvas.drawLine(45f, currentY, rightMargin, currentY, linePaint)
        currentY += 25f

        // Recent Health Readings Summary
        canvas.drawText("آخر القياسات الصحية المسجلة:", rightMargin, currentY, headerPaint)
        currentY += 18f
        if (readings.isEmpty()) {
            canvas.drawText("لا توجد قراءات مسجلة في هذه الفترة.", rightMargin, currentY, textPaint)
            currentY += 16f
        } else {
            val sampleReadings = readings.take(6)
            sampleReadings.forEach { r ->
                val readingText = if (r.value2 != null) {
                    "• ${r.type.labelArabic}: ${r.value1.toInt()}/${r.value2.toInt()} ${r.type.unitArabic} (${r.note})"
                } else {
                    "• ${r.type.labelArabic}: ${r.value1} ${r.type.unitArabic} (${r.note})"
                }
                canvas.drawText(readingText, rightMargin, currentY, textPaint)
                currentY += 16f
            }
        }

        // Disclaimer Footer
        currentY = 790f
        canvas.drawLine(45f, currentY, rightMargin, currentY, linePaint)
        currentY += 18f
        canvas.drawText("تنبيه: بيلّو أداة لتسجيل الالتزام فقط ولا يقدم استشارات أو توصيات طبية.", rightMargin, currentY, mutedPaint)

        document.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Pillo_Doctor_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            file
        } catch (_: Exception) {
            document.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File, title: String = "مشاركة التقرير الطبي") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // Fallback general intent
        }
    }
}

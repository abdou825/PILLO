package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthReading
import com.example.data.model.HealthReadingType

@Composable
fun HealthChartsView(
    type: HealthReadingType,
    readings: List<HealthReading>,
    modifier: Modifier = Modifier
) {
    val sorted = readings.sortedBy { it.timestamp }.takeLast(10)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رسم بياني: ${type.labelArabic} (${type.unitArabic})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (sorted.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "سجل قراءتين على الأقل لإظهار مسار الرسم البياني.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val minVal = (sorted.minOfOrNull { it.value1 } ?: 0f) * 0.85f
                val maxVal = (sorted.maxOfOrNull { it.value1 } ?: 100f) * 1.15f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val points = mutableListOf<Offset>()
                    val stepX = w / (sorted.size - 1)

                    sorted.forEachIndexed { index, item ->
                        val x = index * stepX
                        val normalizedY = (item.value1 - minVal) / range
                        val y = h - (normalizedY * h)
                        points.add(Offset(x, y))
                    }

                    // Line Chart Path
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF0D9488),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Data Dots
                    points.forEach { pt ->
                        drawCircle(color = Color(0xFF0D9488), radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                    }
                }

                Text(
                    text = "آخر قراءة: ${sorted.last().value1} ${type.unitArabic} • السجل لا يقدم تشخيصاً طبياً.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

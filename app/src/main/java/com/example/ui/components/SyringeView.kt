package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SyringeView(
    totalCount: Int,
    remainingCount: Int,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFF3B82F6)
) {
    val total = totalCount.coerceIn(1, 14)
    val remaining = remainingCount.coerceIn(0, total)
    val taken = total - remaining

    val columns = when {
        total <= 5 -> total
        total <= 8 -> 4
        else -> 5
    }
    val rows = (total + columns - 1) / columns

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(columns.toFloat() / (rows.toFloat() * 1.5f).coerceAtLeast(1f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val cellW = w / columns
            val cellH = h / rows

            for (i in 0 until total) {
                val col = i % columns
                val row = i / columns

                val cx = col * cellW + cellW / 2f
                val cy = row * cellH + cellH / 2f

                val isTaken = i >= remaining

                // Syringe dimensions
                val barrelW = cellW * 0.32f
                val barrelH = cellH * 0.55f
                val needleH = cellH * 0.22f
                val plungerH = cellH * 0.14f

                val needleTop = cy - (barrelH + needleH + plungerH) / 2f
                val barrelTop = needleTop + needleH
                val plungerTop = barrelTop + barrelH

                // 1. Needle
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(cx, needleTop),
                    end = Offset(cx, barrelTop),
                    strokeWidth = 2.dp.toPx()
                )

                // 2. Syringe Barrel
                val barrelColor = if (isTaken) Color(0xFFE2E8F0) else Color(0xFFF8FAFC)
                drawRoundRect(
                    color = barrelColor,
                    topLeft = Offset(cx - barrelW / 2f, barrelTop),
                    size = Size(barrelW, barrelH),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(cx - barrelW / 2f, barrelTop),
                    size = Size(barrelW, barrelH),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Liquid fill if remaining
                if (!isTaken) {
                    drawRoundRect(
                        color = fillColor.copy(alpha = 0.8f),
                        topLeft = Offset(cx - barrelW / 2f + 2f, barrelTop + 4f),
                        size = Size(barrelW - 4f, barrelH * 0.65f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // 3. Plunger handle
                val plungerW = barrelW * 0.7f
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(cx, barrelTop + barrelH),
                    end = Offset(cx, plungerTop + plungerH),
                    strokeWidth = 2.dp.toPx()
                )
                drawRoundRect(
                    color = Color(0xFF475569),
                    topLeft = Offset(cx - plungerW / 2f, plungerTop + plungerH - 4.dp.toPx()),
                    size = Size(plungerW, 4.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )

                // 4. If taken, draw big diagonal cross out
                if (isTaken) {
                    val crossSize = cellW * 0.4f
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(cx - crossSize / 2f, cy - crossSize / 2f),
                        end = Offset(cx + crossSize / 2f, cy + crossSize / 2f),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(cx - crossSize / 2f, cy + crossSize / 2f),
                        end = Offset(cx + crossSize / 2f, cy - crossSize / 2f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}

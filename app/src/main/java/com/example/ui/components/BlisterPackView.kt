package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BlisterPackView(
    totalCount: Int,
    remainingCount: Int,
    modifier: Modifier = Modifier,
    pillColor: Color = Color(0xFF0D9488),
    pillSecondaryColor: Color = Color(0xFFFBBF24),
    triggerPopAnimation: Boolean = false
) {
    val count = totalCount.coerceIn(1, 30)
    val remaining = remainingCount.coerceIn(0, count)
    val takenCount = count - remaining

    // Pop-out animation for the most recently taken pill
    val popScale = remember { Animatable(1f) }
    val popAlpha = remember { Animatable(1f) }

    LaunchedEffect(triggerPopAnimation, remainingCount) {
        if (triggerPopAnimation) {
            popScale.snapTo(1f)
            popAlpha.snapTo(1f)
            popScale.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
            popAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    // Grid layout: 2 columns if <= 8, else 3 or 4 columns
    val columns = when {
        count <= 6 -> 3
        count <= 10 -> 2
        count <= 18 -> 3
        else -> 4
    }
    val rows = (count + columns - 1) / columns

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(columns.toFloat() / (rows.toFloat() * 1.35f).coerceAtLeast(1f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw blister foil metallic card background
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(0f, 0f),
                size = Size(width, height),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
            // Foil border rim
            drawRoundRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(2f, 2f),
                size = Size(width - 4f, height - 4f),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // Perforations & texture lines
            val cellWidth = width / columns
            val cellHeight = height / rows

            for (c in 1 until columns) {
                val x = c * cellWidth
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.4f),
                    start = Offset(x, 10f),
                    end = Offset(x, height - 10f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            for (r in 1 until rows) {
                val y = r * cellHeight
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.4f),
                    start = Offset(10f, y),
                    end = Offset(width - 10f, y),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Draw each blister cavity slot
            for (index in 0 until count) {
                val col = index % columns
                val row = index / columns

                val centerX = col * cellWidth + cellWidth / 2f
                val centerY = row * cellHeight + cellHeight / 2f

                val cavityW = cellWidth * 0.72f
                val cavityH = cellHeight * 0.72f

                // Blister transparent bubble pocket
                drawRoundRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(centerX - cavityW / 2f, centerY - cavityH / 2f),
                    size = Size(cavityW, cavityH),
                    cornerRadius = CornerRadius(cavityW / 2f, cavityW / 2f)
                )
                drawRoundRect(
                    color = Color(0xFF94A3B8).copy(alpha = 0.6f),
                    topLeft = Offset(centerX - cavityW / 2f, centerY - cavityH / 2f),
                    size = Size(cavityW, cavityH),
                    cornerRadius = CornerRadius(cavityW / 2f, cavityW / 2f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val isRemaining = index < remaining

                if (isRemaining) {
                    // Pill Capsule inside cavity
                    val pillW = cavityW * 0.75f
                    val pillH = cavityH * 0.65f
                    val pillTop = centerY - pillH / 2f

                    // Half 1: Teal
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(centerX - pillW / 2f, pillTop),
                        size = Size(pillW / 2f, pillH),
                        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
                    )
                    // Half 2: Amber
                    drawRoundRect(
                        color = pillSecondaryColor,
                        topLeft = Offset(centerX, pillTop),
                        size = Size(pillW / 2f, pillH),
                        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
                    )
                    // Capsule shine highlight
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.45f),
                        topLeft = Offset(centerX - pillW * 0.35f, pillTop + pillH * 0.2f),
                        size = Size(pillW * 0.7f, pillH * 0.25f),
                        cornerRadius = CornerRadius(pillH * 0.15f, pillH * 0.15f)
                    )
                } else {
                    // Empty punctured slot foil
                    drawCircle(
                        color = Color(0xFF64748B).copy(alpha = 0.25f),
                        radius = cavityW * 0.22f,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BottleView(
    totalQuantity: Int,
    remainingQuantity: Int,
    modifier: Modifier = Modifier,
    liquidColor: Color = Color(0xFFEF4444)
) {
    val total = totalQuantity.coerceAtLeast(1)
    val remaining = remainingQuantity.coerceIn(0, total)
    val targetRatio = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 600),
        label = "LiquidRatio"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.4f)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val centerX = w / 2f
            val bottleW = w * 0.45f
            val bottleH = h * 0.72f
            val bottleTop = h * 0.24f
            val bottleLeft = centerX - bottleW / 2f

            // 1. Bottle Cap
            val capW = bottleW * 0.42f
            val capH = h * 0.12f
            val capLeft = centerX - capW / 2f
            val capTop = bottleTop - capH

            // Cap ridges
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(capLeft, capTop),
                size = Size(capW, capH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Neck
            val neckW = capW * 0.75f
            val neckH = h * 0.04f
            drawRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(centerX - neckW / 2f, capTop + capH),
                size = Size(neckW, neckH)
            )

            // 2. Bottle Body Glass (outer)
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(bottleLeft, bottleTop),
                size = Size(bottleW, bottleH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // 3. Liquid level
            val liquidMaxH = bottleH * 0.88f
            val currentLiquidH = liquidMaxH * animatedRatio
            val liquidTop = (bottleTop + bottleH) - currentLiquidH

            if (currentLiquidH > 2f) {
                // Liquid fill
                drawRoundRect(
                    color = liquidColor.copy(alpha = 0.82f),
                    topLeft = Offset(bottleLeft + 4.dp.toPx(), liquidTop),
                    size = Size(bottleW - 8.dp.toPx(), currentLiquidH - 4.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                // Liquid top meniscus shine
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(bottleLeft + 8.dp.toPx(), liquidTop),
                    size = Size(bottleW - 16.dp.toPx(), 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // 4. Glass border
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(bottleLeft, bottleTop),
                size = Size(bottleW, bottleH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // 5. Volume measurement gradation tick lines on the side
            val ticks = 5
            for (t in 1 until ticks) {
                val tickY = (bottleTop + bottleH) - (liquidMaxH * (t.toFloat() / ticks))
                val tickLen = if (t % 2 == 0) 18.dp.toPx() else 10.dp.toPx()
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(bottleLeft + 6.dp.toPx(), tickY),
                    end = Offset(bottleLeft + 6.dp.toPx() + tickLen, tickY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Glass reflection highlight
            drawLine(
                color = Color.White.copy(alpha = 0.65f),
                start = Offset(bottleLeft + bottleW * 0.82f, bottleTop + 10.dp.toPx()),
                end = Offset(bottleLeft + bottleW * 0.82f, bottleTop + bottleH - 12.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}

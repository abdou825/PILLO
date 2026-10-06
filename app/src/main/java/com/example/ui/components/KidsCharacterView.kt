package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun KidsCharacterView(
    modifier: Modifier = Modifier,
    isCheering: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "KidsCharacterAnim")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BounceY"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.8f)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f + bounceY

            // 1. Red Superhero Cape in background
            val capePath = Path().apply {
                moveTo(cx - 50.dp.toPx(), cy - 20.dp.toPx())
                lineTo(cx - 75.dp.toPx(), cy + 50.dp.toPx())
                quadraticBezierTo(cx, cy + 65.dp.toPx(), cx + 75.dp.toPx(), cy + 50.dp.toPx())
                lineTo(cx + 50.dp.toPx(), cy - 20.dp.toPx())
                close()
            }
            drawPath(capePath, color = Color(0xFFEF4444))

            // 2. Pill Capsule Body
            val bodyW = 90.dp.toPx()
            val bodyH = 110.dp.toPx()
            val bodyLeft = cx - bodyW / 2f
            val bodyTop = cy - bodyH / 2f

            // Top half: Bright Teal
            drawRoundRect(
                color = Color(0xFF06B6D4),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyW, bodyH / 2f + 5.dp.toPx()),
                cornerRadius = CornerRadius(bodyW / 2f, bodyW / 2f)
            )

            // Bottom half: Sunny Yellow
            drawRoundRect(
                color = Color(0xFFFBBF24),
                topLeft = Offset(bodyLeft, bodyTop + bodyH / 2f),
                size = Size(bodyW, bodyH / 2f),
                cornerRadius = CornerRadius(bodyW / 2f, bodyW / 2f)
            )

            // Body outline
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(bodyW / 2f, bodyW / 2f),
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Cute Cartoon Eyes
            val eyeRadius = 7.dp.toPx()
            val eyeSpacing = 16.dp.toPx()
            val eyeY = bodyTop + bodyH * 0.38f

            // Left eye
            drawCircle(Color.Black, radius = eyeRadius, center = Offset(cx - eyeSpacing, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(cx - eyeSpacing - 2f, eyeY - 2f))

            // Right eye
            drawCircle(Color.Black, radius = eyeRadius, center = Offset(cx + eyeSpacing, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(cx + eyeSpacing - 2f, eyeY - 2f))

            // Rosy Cheeks
            drawCircle(Color(0xFFFB7185).copy(alpha = 0.65f), radius = 6.dp.toPx(), center = Offset(cx - eyeSpacing - 10.dp.toPx(), eyeY + 8.dp.toPx()))
            drawCircle(Color(0xFFFB7185).copy(alpha = 0.65f), radius = 6.dp.toPx(), center = Offset(cx + eyeSpacing + 10.dp.toPx(), eyeY + 8.dp.toPx()))

            // Big Happy Smile
            val smilePath = Path().apply {
                moveTo(cx - 14.dp.toPx(), eyeY + 8.dp.toPx())
                quadraticBezierTo(cx, eyeY + 22.dp.toPx(), cx + 14.dp.toPx(), eyeY + 8.dp.toPx())
            }
            drawPath(smilePath, color = Color(0xFF0F172A), style = Stroke(width = 3.dp.toPx()))

            // 4. Little Golden Stars around character
            fun drawStar(starX: Float, starY: Float, starSize: Float) {
                drawCircle(Color(0xFFFDE047), radius = starSize, center = Offset(starX, starY))
                drawCircle(Color.White, radius = starSize * 0.4f, center = Offset(starX, starY))
            }

            drawStar(cx - 65.dp.toPx(), cy - 35.dp.toPx(), 7.dp.toPx())
            drawStar(cx + 65.dp.toPx(), cy - 25.dp.toPx(), 8.dp.toPx())
            drawStar(cx + 50.dp.toPx(), cy + 45.dp.toPx(), 6.dp.toPx())
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.PetSpecies

@Composable
fun PetCanvasView(
    species: String = PetSpecies.BUNNY.id,
    stage: Int = 1,
    happiness: Int = 80,
    equippedItem: String = "none",
    size: Dp = 220.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_anim")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f + bounceOffset

            // Shadow under pet
            drawOval(
                color = Color(0x22000000),
                topLeft = Offset(centerX - 60f, canvasHeight - 24f),
                size = Size(120f, 20f)
            )

            // Stage scale factor (1 = baby, 4 = champion)
            val scale = when (stage) {
                1 -> 0.75f
                2 -> 0.90f
                3 -> 1.05f
                else -> 1.20f
            }

            val isTired = happiness < 30

            when (species) {
                PetSpecies.KITTY.id -> drawKitty(centerX, centerY, scale, eyeBlink, isTired)
                PetSpecies.FALCON.id -> drawFalcon(centerX, centerY, scale, eyeBlink, isTired)
                else -> drawBunny(centerX, centerY, scale, eyeBlink, isTired)
            }

            // Equipped Accessory
            drawAccessory(equippedItem, centerX, centerY, scale)

            // Happiness particles (Hearts or sparkles)
            if (happiness > 70) {
                drawHeartParticle(centerX + 65f * scale, centerY - 65f * scale)
                drawStarParticle(centerX - 65f * scale, centerY - 55f * scale)
            }
        }
    }
}

private fun DrawScope.drawBunny(centerX: Float, centerY: Float, scale: Float, blink: Float, isTired: Boolean) {
    val bodyRadius = 55f * scale
    val headRadius = 45f * scale
    val primaryColor = Color(0xFFFDE047) // Warm golden yellow
    val bellyColor = Color(0xFFFEF08A)
    val earInside = Color(0xFFF472B6)

    // Ears
    drawOval(
        color = primaryColor,
        topLeft = Offset(centerX - 35f * scale, centerY - 120f * scale),
        size = Size(24f * scale, 70f * scale)
    )
    drawOval(
        color = earInside,
        topLeft = Offset(centerX - 30f * scale, centerY - 110f * scale),
        size = Size(14f * scale, 50f * scale)
    )

    drawOval(
        color = primaryColor,
        topLeft = Offset(centerX + 11f * scale, centerY - 120f * scale),
        size = Size(24f * scale, 70f * scale)
    )
    drawOval(
        color = earInside,
        topLeft = Offset(centerX + 16f * scale, centerY - 110f * scale),
        size = Size(14f * scale, 50f * scale)
    )

    // Body
    drawCircle(
        color = primaryColor,
        radius = bodyRadius,
        center = Offset(centerX, centerY + 20f * scale)
    )
    drawCircle(
        color = bellyColor,
        radius = bodyRadius * 0.65f,
        center = Offset(centerX, centerY + 25f * scale)
    )

    // Head
    drawCircle(
        color = primaryColor,
        radius = headRadius,
        center = Offset(centerX, centerY - 30f * scale)
    )

    // Cheeks
    drawCircle(
        color = Color(0x66F43F5E),
        radius = 12f * scale,
        center = Offset(centerX - 26f * scale, centerY - 20f * scale)
    )
    drawCircle(
        color = Color(0x66F43F5E),
        radius = 12f * scale,
        center = Offset(centerX + 26f * scale, centerY - 20f * scale)
    )

    // Eyes
    val eyeHeight = if (isTired) 4f * scale else 10f * scale * blink
    drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerX - 20f * scale, centerY - 38f * scale),
        size = Size(8f * scale, eyeHeight)
    )
    drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerX + 12f * scale, centerY - 38f * scale),
        size = Size(8f * scale, eyeHeight)
    )

    // Cute Nose and Mouth
    drawCircle(
        color = Color(0xFFE11D48),
        radius = 4f * scale,
        center = Offset(centerX, centerY - 25f * scale)
    )
}

private fun DrawScope.drawKitty(centerX: Float, centerY: Float, scale: Float, blink: Float, isTired: Boolean) {
    val headRadius = 48f * scale
    val primaryColor = Color(0xFFFB923C) // Orange tabby
    val bellyColor = Color(0xFFFED7AA)

    // Cat Body
    drawCircle(
        color = primaryColor,
        radius = 56f * scale,
        center = Offset(centerX, centerY + 20f * scale)
    )
    drawCircle(
        color = bellyColor,
        radius = 35f * scale,
        center = Offset(centerX, centerY + 22f * scale)
    )

    // Triangle Ears
    val leftEar = Path().apply {
        moveTo(centerX - 42f * scale, centerY - 30f * scale)
        lineTo(centerX - 20f * scale, centerY - 80f * scale)
        lineTo(centerX - 2f * scale, centerY - 45f * scale)
        close()
    }
    drawPath(leftEar, color = primaryColor)

    val rightEar = Path().apply {
        moveTo(centerX + 42f * scale, centerY - 30f * scale)
        lineTo(centerX + 20f * scale, centerY - 80f * scale)
        lineTo(centerX + 2f * scale, centerY - 45f * scale)
        close()
    }
    drawPath(rightEar, color = primaryColor)

    // Head
    drawCircle(
        color = primaryColor,
        radius = headRadius,
        center = Offset(centerX, centerY - 25f * scale)
    )

    // Eyes
    val eyeHeight = if (isTired) 3f * scale else 9f * scale * blink
    drawOval(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX - 22f * scale, centerY - 32f * scale),
        size = Size(9f * scale, eyeHeight)
    )
    drawOval(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX + 13f * scale, centerY - 32f * scale),
        size = Size(9f * scale, eyeHeight)
    )

    // Whiskers
    drawLine(Color(0xFF334155), Offset(centerX - 45f * scale, centerY - 22f * scale), Offset(centerX - 20f * scale, centerY - 20f * scale), 2f)
    drawLine(Color(0xFF334155), Offset(centerX + 20f * scale, centerY - 20f * scale), Offset(centerX + 45f * scale, centerY - 22f * scale), 2f)
}

private fun DrawScope.drawFalcon(centerX: Float, centerY: Float, scale: Float, blink: Float, isTired: Boolean) {
    val primaryColor = Color(0xFF38BDF8) // Sky blue noble falcon
    val wingColor = Color(0xFF0284C7)
    val beakColor = Color(0xFFF59E0B)

    // Body
    drawCircle(
        color = primaryColor,
        radius = 54f * scale,
        center = Offset(centerX, centerY + 18f * scale)
    )

    // Wings
    drawOval(
        color = wingColor,
        topLeft = Offset(centerX - 70f * scale, centerY - 5f * scale),
        size = Size(35f * scale, 60f * scale)
    )
    drawOval(
        color = wingColor,
        topLeft = Offset(centerX + 35f * scale, centerY - 5f * scale),
        size = Size(35f * scale, 60f * scale)
    )

    // Head
    drawCircle(
        color = primaryColor,
        radius = 45f * scale,
        center = Offset(centerX, centerY - 30f * scale)
    )

    // Feather Crown
    drawCircle(
        color = wingColor,
        radius = 12f * scale,
        center = Offset(centerX, centerY - 72f * scale)
    )

    // Eyes
    val eyeHeight = if (isTired) 4f * scale else 8f * scale * blink
    drawCircle(Color.White, 9f * scale, Offset(centerX - 18f * scale, centerY - 36f * scale))
    drawCircle(Color(0xFF0F172A), 5f * scale, Offset(centerX - 18f * scale, centerY - 36f * scale))
    drawCircle(Color.White, 9f * scale, Offset(centerX + 18f * scale, centerY - 36f * scale))
    drawCircle(Color(0xFF0F172A), 5f * scale, Offset(centerX + 18f * scale, centerY - 36f * scale))

    // Beak
    val beak = Path().apply {
        moveTo(centerX - 10f * scale, centerY - 24f * scale)
        lineTo(centerX + 10f * scale, centerY - 24f * scale)
        lineTo(centerX, centerY - 10f * scale)
        close()
    }
    drawPath(beak, color = beakColor)
}

private fun DrawScope.drawAccessory(item: String, centerX: Float, centerY: Float, scale: Float) {
    when (item) {
        "doctor_cap" -> {
            // Little doctor mirror / cap
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(centerX - 24f * scale, centerY - 85f * scale),
                size = Size(48f * scale, 20f * scale),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Red cross on cap
            drawLine(Color(0xFFEF4444), Offset(centerX, centerY - 82f * scale), Offset(centerX, centerY - 68f * scale), 4f)
            drawLine(Color(0xFFEF4444), Offset(centerX - 7f * scale, centerY - 75f * scale), Offset(centerX + 7f * scale, centerY - 75f * scale), 4f)
        }
        "glasses" -> {
            // Cool glasses
            drawRoundRect(
                color = Color(0xAA1E293B),
                topLeft = Offset(centerX - 32f * scale, centerY - 45f * scale),
                size = Size(26f * scale, 18f * scale),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xAA1E293B),
                topLeft = Offset(centerX + 6f * scale, centerY - 45f * scale),
                size = Size(26f * scale, 18f * scale),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawLine(Color(0xFF1E293B), Offset(centerX - 6f * scale, centerY - 36f * scale), Offset(centerX + 6f * scale, centerY - 36f * scale), 3f)
        }
        "scarf" -> {
            // Hero scarf
            drawRoundRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(centerX - 35f * scale, centerY + 10f * scale),
                size = Size(70f * scale, 18f * scale),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
    }
}

private fun DrawScope.drawHeartParticle(x: Float, y: Float) {
    val path = Path().apply {
        moveTo(x, y)
        cubicTo(x - 10f, y - 10f, x - 20f, y + 5f, x, y + 20f)
        cubicTo(x + 20f, y + 5f, x + 10f, y - 10f, x, y)
        close()
    }
    drawPath(path, color = Color(0xFFF43F5E))
}

private fun DrawScope.drawStarParticle(x: Float, y: Float) {
    drawCircle(Color(0xFFFBBF24), 8f, Offset(x, y))
}

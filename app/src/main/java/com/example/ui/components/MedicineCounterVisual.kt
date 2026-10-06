package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medicine
import com.example.data.model.MedicineType
import kotlin.math.ceil

@Composable
fun MedicineCounterVisual(
    medicine: Medicine,
    modifier: Modifier = Modifier,
    triggerAnimation: Boolean = false,
    bodyFontSize: Int = 16
) {
    val total = medicine.totalQuantity
    val remaining = medicine.remainingQuantity
    val timesPerDay = medicine.timesPerDay.coerceAtLeast(1)
    val daysLeft = ceil(remaining.toDouble() / timesPerDay.toDouble()).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (medicine.type) {
            MedicineType.PILLS -> {
                BlisterPackView(
                    totalCount = total,
                    remainingCount = remaining,
                    triggerPopAnimation = triggerAnimation
                )
            }
            MedicineType.SYRUP -> {
                BottleView(
                    totalQuantity = total,
                    remainingQuantity = remaining,
                    liquidColor = Color(0xFFE11D48)
                )
            }
            MedicineType.DROPS -> {
                BottleView(
                    totalQuantity = total,
                    remainingQuantity = remaining,
                    liquidColor = Color(0xFF0284C7)
                )
            }
            MedicineType.INJECTION -> {
                SyringeView(
                    totalCount = total,
                    remainingCount = remaining
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress text: "فاضل X جرعة (حوالي Y يوم)"
        val remainingText = if (remaining <= 0) {
            "العلاج خلص تماماً"
        } else {
            "فاضل $remaining جرعة (حوالي $daysLeft ${if (daysLeft == 1) "يوم" else "أيام"})"
        }

        Text(
            text = remainingText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = bodyFontSize.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (remaining <= timesPerDay * 2) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

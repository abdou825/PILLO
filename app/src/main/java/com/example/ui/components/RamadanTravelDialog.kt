package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile

@Composable
fun RamadanTravelDialog(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var ramadanEnabled by remember { mutableStateOf(userProfile.ramadanModeEnabled) }
    var suhoorTime by remember { mutableStateOf(userProfile.suhoorTime) }
    var iftarTime by remember { mutableStateOf(userProfile.iftarTime) }

    var travelOffset by remember { mutableStateOf(userProfile.travelTimeZoneOffsetHours) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("وضع رمضان والسفر 🌙✈️", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Safety note
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "ملاحظة أمان: لو الدوا حساس للمواعيد اسأل دكتورك قبل تغيير الجدول.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B)),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // 1. Ramadan Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تفعيل جدول شهر رمضان", fontWeight = FontWeight.Bold)
                        Text("جدولة الجرعات بين الإفطار والسحور فقط", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = ramadanEnabled, onCheckedChange = { ramadanEnabled = it })
                }

                if (ramadanEnabled) {
                    TimeStepperField(label = "ميعاد الإفطار (المغرب)", value = iftarTime, onValueChange = { iftarTime = it })
                    TimeStepperField(label = "ميعاد السحور (الفجر)", value = suhoorTime, onValueChange = { suhoorTime = it })
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Travel Mode
                Text("وضع السفر وفرق التوقيت:", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { travelOffset = 0 },
                        colors = if (travelOffset == 0) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("توقيت بلدي (الأصل)")
                    }
                    Button(
                        onClick = { travelOffset = 1 },
                        colors = if (travelOffset != 0) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("التوقيت الجديد (+1س)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = userProfile.copy(
                        ramadanModeEnabled = ramadanEnabled,
                        suhoorTime = suhoorTime,
                        iftarTime = iftarTime,
                        travelTimeZoneOffsetHours = travelOffset
                    )
                    onSaveProfile(updated)
                    onDismiss()
                }
            ) {
                Text("تطبيق وتحديث الجدول")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

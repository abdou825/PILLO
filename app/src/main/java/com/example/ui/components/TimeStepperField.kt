package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

fun adjustTimeByMinutes(timeStr: String, deltaMinutes: Int): String {
    val parts = timeStr.trim().split(":")
    if (parts.size != 2) return "08:00"
    val h = parts[0].toIntOrNull() ?: 8
    val m = parts[1].toIntOrNull() ?: 0
    val totalMinutes = ((h * 60 + m + deltaMinutes) % 1440 + 1440) % 1440
    val newH = totalMinutes / 60
    val newM = totalMinutes % 60
    return String.format(Locale.US, "%02d:%02d", newH, newM)
}

@Composable
fun TimeStepperField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editDialogText by remember { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onValueChange(adjustTimeByMinutes(value, -30)) },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("time_minus_${label}")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "تقليل 30 دقيقة",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable {
                        editDialogText = value
                        showEditDialog = true
                    }
                    .testTag("time_display_${label}")
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            IconButton(
                onClick = { onValueChange(adjustTimeByMinutes(value, 30)) },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("time_plus_${label}")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "زيادة 30 دقيقة",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                showEditDialog = false
            },
            title = { Text("تعديل $label") },
            text = {
                Column {
                    Text("اكتب الميعاد بتوقيت 24 ساعة (مثال: 08:30):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDialogText,
                        onValueChange = {
                            if (it.length <= 5) editDialogText = it
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (editDialogText.isNotBlank()) {
                            onValueChange(editDialogText.trim())
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        showEditDialog = false
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

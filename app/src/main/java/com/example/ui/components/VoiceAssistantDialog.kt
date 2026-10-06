package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.VoiceAssistantHelper

@Composable
fun VoiceAssistantDialog(
    onTakeNextDose: () -> Unit,
    onSnooze: (Int) -> Unit,
    onRemainingQuery: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("جاري الاستماع... تحدث بالعامية المصرية") }

    val voiceHelper = remember {
        VoiceAssistantHelper(
            context = context,
            onCommandRecognized = { cmd ->
                when (cmd) {
                    is VoiceAssistantHelper.VoiceCommand.TakeDose -> {
                        statusText = "تم التعرف: 'أخدت الدوا' ✓"
                        onTakeNextDose()
                        onDismiss()
                    }
                    is VoiceAssistantHelper.VoiceCommand.Snooze -> {
                        statusText = "تم التعرف: 'تأجيل الجرعة' ✓"
                        onSnooze(cmd.minutes)
                        onDismiss()
                    }
                    is VoiceAssistantHelper.VoiceCommand.RemainingQuantityQuery -> {
                        statusText = "تم التعرف: استعلام عن الرصيد المتبقي ✓"
                        onRemainingQuery()
                        onDismiss()
                    }
                    is VoiceAssistantHelper.VoiceCommand.Unrecognized -> {
                        statusText = "الكلمة المسموعة: '${cmd.rawText}'. حاول قول 'أخدت الدوا' أو 'فكرني بعد ربع ساعة'."
                    }
                }
            },
            onError = { err ->
                statusText = err
            }
        )
    }

    DisposableEffect(Unit) {
        voiceHelper.startListening()
        onDispose {
            voiceHelper.stopListening()
            voiceHelper.destroy()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "استماع صوتي",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "المساعد الصوتي بيلّو 🎙️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "أوامر سريعة مقترحة:\n• 'أخدت الدوا'\n• 'فكرني بعد ربع ساعة'\n• 'فاضل كام من الدوا'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("إلغاء")
            }
        }
    )
}

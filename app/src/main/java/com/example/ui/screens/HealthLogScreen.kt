package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthReading
import com.example.data.model.HealthReadingType
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import com.example.media.PdfReportGenerator
import com.example.ui.components.HealthChartsView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthLogScreen(
    userProfile: UserProfile,
    activeMedicines: List<Medicine>,
    finishedMedicines: List<Medicine>,
    readings: List<HealthReading>,
    onAddReading: (HealthReading) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTypeToAdd by remember { mutableStateOf(HealthReadingType.BLOOD_PRESSURE) }
    var val1Text by remember { mutableStateOf("") }
    var val2Text by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("عشوائي") }

    // Quick Water counter
    var todayWaterCups by remember { mutableIntStateOf(readings.count { it.type == HealthReadingType.WATER }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل صحتي ومتابعة القراءات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // PDF Doctor Report Button (Feature 12)
                    IconButton(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateDoctorReportPdf(
                                context = context,
                                profile = userProfile,
                                medicines = activeMedicines,
                                readings = readings,
                                missedDoseCount = 2,
                                adherencePercent = 95
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.sharePdf(context, pdfFile, "تقرير طبي للطبيب - بيلّو")
                            } else {
                                Toast.makeText(context, "تعذر إنشاء ملف PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("generate_doctor_report_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "تقرير للطبيب", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Doctor Report Highlight Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جاهز لزيارة الدكتور؟",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "اضغط زر 'تقرير للطبيب' لتوليد تقرير PDF شامل وجاهز للطباعة أو المشاركة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Button(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateDoctorReportPdf(
                                context = context,
                                profile = userProfile,
                                medicines = activeMedicines,
                                readings = readings,
                                missedDoseCount = 2,
                                adherencePercent = 95
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.sharePdf(context, pdfFile, "تقرير طبي للطبيب - بيلّو")
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تقرير PDF")
                    }
                }
            }

            // Quick Water Counter Card (Feature 11)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "شرب الماء اليوم: $todayWaterCups أكواب",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "المعدل الصحي الموصى به: 8 أكواب",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (todayWaterCups > 0) todayWaterCups--
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل")
                        }
                        IconButton(
                            onClick = {
                                todayWaterCups++
                                onAddReading(
                                    HealthReading(
                                        profileId = userProfile.id,
                                        type = HealthReadingType.WATER,
                                        value1 = 1f,
                                        note = "كوب ماء"
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }
                }
            }

            // Quick Add Readings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القياسات الصحية",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_reading_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تسجيل قياس جديد")
                }
            }

            // Charts for Readings
            HealthChartsView(
                type = HealthReadingType.BLOOD_PRESSURE,
                readings = readings.filter { it.type == HealthReadingType.BLOOD_PRESSURE }
            )

            HealthChartsView(
                type = HealthReadingType.BLOOD_SUGAR,
                readings = readings.filter { it.type == HealthReadingType.BLOOD_SUGAR }
            )

            HealthChartsView(
                type = HealthReadingType.WEIGHT,
                readings = readings.filter { it.type == HealthReadingType.WEIGHT }
            )

            // History of Finished Treatments
            Text(
                text = "العلاجات المكتملة (${finishedMedicines.size})",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 10.dp)
            )

            if (finishedMedicines.isEmpty()) {
                Text(
                    text = "لا توجد علاجات منتهية بعد. عندما تنتهي من دورة علاج ستظهر هنا مع شهادة إتمامها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                finishedMedicines.forEach { finished ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = finished.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "تم إتمام العلاج بنجاح ✓",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Add Reading Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسجيل قياس صحي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("نوع القياس:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(HealthReadingType.BLOOD_PRESSURE, HealthReadingType.BLOOD_SUGAR, HealthReadingType.WEIGHT).forEach { t ->
                            FilterChip(
                                selected = selectedTypeToAdd == t,
                                onClick = { selectedTypeToAdd = t },
                                label = { Text(t.labelArabic, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (selectedTypeToAdd == HealthReadingType.BLOOD_PRESSURE) {
                        OutlinedTextField(
                            value = val1Text,
                            onValueChange = { val1Text = it },
                            label = { Text("الانقباضي (العالي - مثلاً 120)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = val2Text,
                            onValueChange = { val2Text = it },
                            label = { Text("الانبساطي (الواطي - مثلاً 80)") },
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = val1Text,
                            onValueChange = { val1Text = it },
                            label = { Text("القيمة (${selectedTypeToAdd.unitArabic})") },
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("ملاحظة (صائم / بعد الأكل / طبيعي)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v1 = val1Text.toFloatOrNull() ?: 120f
                        val v2 = val2Text.toFloatOrNull()
                        onAddReading(
                            HealthReading(
                                profileId = userProfile.id,
                                type = selectedTypeToAdd,
                                value1 = v1,
                                value2 = v2,
                                note = noteText.trim()
                            )
                        )
                        showAddDialog = false
                        val1Text = ""
                        val2Text = ""
                    }
                ) {
                    Text("حفظ القياس")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealRelation
import com.example.data.model.Medicine
import com.example.data.model.MedicineType
import com.example.data.model.UserProfile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import com.example.domain.ScheduleCalculator
import com.example.media.MedicineBoxScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    medicineToEdit: Medicine?,
    userProfile: UserProfile,
    onSave: (Medicine) -> Unit,
    onDelete: ((Medicine) -> Unit)? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(medicineToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(medicineToEdit?.type ?: MedicineType.PILLS) }
    var totalQuantity by remember { mutableIntStateOf(medicineToEdit?.totalQuantity ?: 20) }
    var doseAmount by remember { mutableStateOf(medicineToEdit?.doseAmount ?: "1 قرص") }
    var timesPerDay by remember { mutableIntStateOf(medicineToEdit?.timesPerDay ?: 1) }
    var selectedMealRelation by remember { mutableStateOf(medicineToEdit?.mealRelation ?: MealRelation.AFTER_MEAL) }
    val focusManager = LocalFocusManager.current

    // Calculated / editable dose times list
    val calculatedTimes = remember { mutableStateListOf<String>() }

    // Initial populate or recalculate when timesPerDay or mealRelation changes
    LaunchedEffect(timesPerDay, selectedMealRelation) {
        if (medicineToEdit != null && calculatedTimes.isEmpty() && medicineToEdit.customScheduleTimes.isNotEmpty()) {
            calculatedTimes.clear()
            calculatedTimes.addAll(medicineToEdit.customScheduleTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        } else {
            val fresh = ScheduleCalculator.calculateDoseTimes(timesPerDay, selectedMealRelation, userProfile)
            calculatedTimes.clear()
            calculatedTimes.addAll(fresh)
        }
    }

    var editTimeDialogIndex by remember { mutableStateOf<Int?>(null) }
    var editingTimeValue by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val result = MedicineBoxScanner.scanMedicineBox(bitmap ?: android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888))
        if (result.success) {
            name = result.detectedName
            scanMessage = result.message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (medicineToEdit == null) "إضافة دواء جديد" else "تعديل الدواء",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (medicineToEdit != null && onDelete != null) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onDelete(medicineToEdit)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الدواء", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Medicine Name + Camera Box Scan Button (Feature 15)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                            scanMessage = null
                        },
                        label = { Text("اسم الدواء") },
                        placeholder = { Text("مثلاً: أوجمنتين، بانادول...") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("من فضلك اكتب اسم الدواء", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("medicine_name_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            cameraLauncher.launch(null)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("scan_medicine_camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "مسح علبة الدواء بالكاميرا")
                    }
                }

                if (scanMessage != null) {
                    Text(
                        text = scanMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 2. Medicine Type
            Column {
                Text(
                    text = "نوع العلاج",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedicineType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (doseAmount == "1 قرص" && type == MedicineType.SYRUP) doseAmount = "5 مل"
                                if (doseAmount == "5 مل" && type == MedicineType.PILLS) doseAmount = "1 قرص"
                                if (type == MedicineType.DROPS) doseAmount = "3 نقط"
                                if (type == MedicineType.INJECTION) doseAmount = "1 أمبول"
                            },
                            label = { Text(type.labelArabic, fontSize = 12.sp) },
                            modifier = Modifier.testTag("type_chip_${type.name}")
                        )
                    }
                }
            }

            // 3. Dose Amount & Total Quantity in Package
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = doseAmount,
                    onValueChange = { doseAmount = it },
                    label = { Text("مقدار الجرعة") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )

                // Total Quantity Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الكمية في العلبة",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (totalQuantity > 1) totalQuantity-- },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل")
                        }
                        Text(
                            text = "$totalQuantity",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { totalQuantity++ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }
                }
            }

            // 4. Times Per Day (1 to 6)
            Column {
                Text(
                    text = "كم مرة في اليوم؟ ($timesPerDay مرات)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..6) {
                        FilterChip(
                            selected = timesPerDay == i,
                            onClick = { timesPerDay = i },
                            label = { Text("$i") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Meal Relation
            Column {
                Text(
                    text = "ارتباط الجرعة بالأكل",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                MealRelation.values().forEach { relation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { selectedMealRelation = relation },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMealRelation == relation) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = relation.labelArabic,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            if (selectedMealRelation == relation) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 6. Calculated Dose Times Preview (Feature 2 Requirement)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مواعيد الجرعات المحسوبة تلقائياً:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "تقدر تضغط على أي ميعاد وتعدله حسب رغبتك:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        calculatedTimes.forEachIndexed { index, timeStr ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clickable {
                                        editTimeDialogIndex = index
                                        editingTimeValue = timeStr
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val finalTimesString = calculatedTimes.joinToString(",")
                    val remaining = medicineToEdit?.remainingQuantity ?: totalQuantity

                    val medicine = (medicineToEdit ?: Medicine(
                        name = name.trim(),
                        type = selectedType,
                        totalQuantity = totalQuantity,
                        remainingQuantity = remaining,
                        doseAmount = doseAmount,
                        timesPerDay = timesPerDay,
                        mealRelation = selectedMealRelation,
                        customScheduleTimes = finalTimesString
                    )).copy(
                        name = name.trim(),
                        type = selectedType,
                        totalQuantity = totalQuantity,
                        doseAmount = doseAmount,
                        timesPerDay = timesPerDay,
                        mealRelation = selectedMealRelation,
                        customScheduleTimes = finalTimesString
                    )

                    onSave(medicine)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp)
                    .testTag("save_medicine_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "حفظ ومزامنة المنبه",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Time Edit Dialog
    if (editTimeDialogIndex != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editTimeDialogIndex = null },
            title = { Text("تعديل ميعاد الجرعة") },
            text = {
                Column {
                    Text("اكتب الميعاد بنظام 24 ساعة (مثال: 08:30):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingTimeValue,
                        onValueChange = { editingTimeValue = it },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = editTimeDialogIndex
                        if (idx != null && editingTimeValue.isNotBlank()) {
                            calculatedTimes[idx] = editingTimeValue.trim()
                        }
                        editTimeDialogIndex = null
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editTimeDialogIndex = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

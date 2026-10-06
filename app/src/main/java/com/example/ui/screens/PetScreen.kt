package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeMode
import com.example.data.model.CoinWallet
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetSpecies
import com.example.data.model.PetState
import com.example.ui.components.PetCanvasView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    petState: PetState?,
    coinWallet: CoinWallet?,
    unlockedItems: List<String>,
    ageMode: AgeMode,
    onFeedPet: () -> Unit,
    onChangeSpecies: (String) -> Unit,
    onEquipItem: (String) -> Unit,
    onUnlockItem: (itemId: String, cost: Int, name: String) -> Unit,
    onBack: () -> Unit
) {
    val pet = petState ?: PetState()
    val isSenior = ageMode == AgeMode.SENIORS
    val isKids = ageMode == AgeMode.KIDS

    val stageInfo = when (pet.stage) {
        1 -> PetGrowthStage.BABY
        2 -> PetGrowthStage.CHILD
        3 -> PetGrowthStage.YOUTH
        else -> PetGrowthStage.CHAMPION
    }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أليف بيلّو الافتراضي 🐾", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${coinWallet?.balance ?: 0}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage & Growth Header
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "مرحلة النمو: ${stageInfo.titleArabic}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF166534),
                            fontSize = if (isSenior) 18.sp else 16.sp
                        )
                        Text(
                            text = "كل جرعة بتاخدها بتغذي أليفك وبتخليه يكبر ويفرح!",
                            color = Color(0xFF15803D),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFDCFCE7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF16A34A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Pet Canvas View
            PetCanvasView(
                species = pet.species,
                stage = pet.stage,
                happiness = pet.happiness,
                equippedItem = pet.equippedItem,
                size = if (isSenior) 260.dp else 220.dp
            )

            // Happiness Bar
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مستوى النشاط والسعادة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${pet.happiness}% ❤️", fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (pet.happiness / 150f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(10.dp),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFE2E8F0),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feed Action
            Button(
                onClick = {
                    onFeedPet()
                    feedbackMessage = "تم إطعام أليفك بنجاح! فرحان وبيدعيلك بالصحة 🌟"
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إطعام أليفك (تفاحة صحية 🍎)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (feedbackMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(feedbackMessage!!, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Species Picker (Store / Switcher)
            Text(
                text = "اختر نوع الأليف المفضل 🐾",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetSpecies.entries.forEach { sp ->
                    val isUnlocked = sp.unlockCost == 0 || unlockedItems.contains(sp.id)
                    val isSelected = pet.species == sp.id

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (isUnlocked) {
                                    onChangeSpecies(sp.id)
                                } else {
                                    onUnlockItem(sp.id, sp.unlockCost, sp.labelArabic)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(sp.labelArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD97706))
                                    Text("${sp.unlockCost} 🪙", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                                }
                            } else if (isSelected) {
                                Text("نشط ✓", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accessories Wardrobe
            Text(
                text = "إكسسوارات الأبطال (افتحها بنقاطك) 👒",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            val accessories = listOf(
                Triple("doctor_cap", "قبعة الطبيب 👨‍⚕️", 30),
                Triple("glasses", "نظارة شمسية 🕶️", 25),
                Triple("scarf", "وشاح الأبطال 🧣", 20)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accessories.forEach { (id, name, cost) ->
                    val isUnlocked = unlockedItems.contains(id)
                    val isEquipped = pet.equippedItem == id

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (isUnlocked) {
                                    onEquipItem(if (isEquipped) "none" else id)
                                } else {
                                    onUnlockItem(id, cost, name)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF16A34A)) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD97706))
                                    Text("$cost 🪙", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(if (isEquipped) "ملبوس ✓" else "لبس", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

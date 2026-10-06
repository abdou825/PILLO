package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeMode
import com.example.data.model.CoinWallet
import com.example.data.model.DailyChallengeProgress
import com.example.data.model.GameScore
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    ageMode: AgeMode,
    coinWallet: CoinWallet?,
    dailyChallenge: DailyChallengeProgress?,
    highScores: List<GameScore>,
    onCompleteDailyChallenge: () -> Unit,
    onSaveGameScore: (gameId: String, score: Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var continuousPlayMinutes by remember { mutableIntStateOf(0) }
    var showBreakReminderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            continuousPlayMinutes++
            if (continuousPlayMinutes >= 20) {
                showBreakReminderDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ألعاب بيلّو التنافسية 🎮", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${coinWallet?.balance ?: 0} عملة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
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
        ) {
            // Game Selection Tabs (Subway Surfer, Math Blitz, Hardcore Memory, Hyper Sorter)
            ScrollableTabRow(
                selectedTabIndex = selectedGameIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedGameIndex == 0,
                    onClick = { selectedGameIndex = 0 },
                    text = { Text("🏃 صب واي بيلّو", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedGameIndex == 1,
                    onClick = { selectedGameIndex = 1 },
                    text = { Text("🔢 الرياضيات السريعة", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedGameIndex == 2,
                    onClick = { selectedGameIndex = 2 },
                    text = { Text("🧠 الذاكرة الصعبة", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedGameIndex == 3,
                    onClick = { selectedGameIndex = 3 },
                    text = { Text("⚡ فرز الحبوب الناري", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedGameIndex) {
                0 -> PilloSubwayRunnerGame(
                    onGameOver = { score -> onSaveGameScore("game_subway_runner", score) }
                )
                1 -> FastMathCalculationGame(
                    onGameOver = { score -> onSaveGameScore("game_fast_math", score) }
                )
                2 -> HardcoreMemoryMatrixGame(
                    onGameOver = { score -> onSaveGameScore("game_hardcore_memory", score) }
                )
                3 -> HyperPillSorterGame(
                    onGameOver = { score -> onSaveGameScore("game_hyper_sorter", score) }
                )
            }
        }
    }

    if (showBreakReminderDialog) {
        AlertDialog(
            onDismissRequest = { showBreakReminderDialog = false },
            title = { Text("راحة للعينين! 🌿", fontWeight = FontWeight.Bold) },
            text = { Text("لعبت 20 دقيقة متواصلة. ارح عينيك واشرب مية وارجع بتركيز أعلى!") },
            confirmButton = {
                Button(onClick = {
                    showBreakReminderDialog = false
                    continuousPlayMinutes = 0
                }) {
                    Text("تمام، ريحت!")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// GAME 1: SUBWAY SURFER ENDLESS RUNNER (صعوبة متزايدة ومراحل لا تتكرر)
// -------------------------------------------------------------
@Composable
fun PilloSubwayRunnerGame(onGameOver: (Int) -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var currentLane by remember { mutableIntStateOf(1) } // 0: Left, 1: Center, 2: Right
    var isJumping by remember { mutableStateOf(false) }
    var isSliding by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var gameSpeed by remember { mutableFloatStateOf(1.0f) }

    data class RunnerObstacle(val id: Int, var lane: Int, var y: Float, val type: Int)
    val obstacles = remember { mutableStateListOf<RunnerObstacle>() }
    val coins = remember { mutableStateListOf<RunnerObstacle>() }

    fun startNewGame() {
        score = 0
        coinsCollected = 0
        currentLane = 1
        isJumping = false
        isSliding = false
        gameOver = false
        gameSpeed = 1.0f
        obstacles.clear()
        coins.clear()

        // Seed initial procedural obstacles
        var startY = -200f
        for (i in 0..8) {
            val randomLane = Random.nextInt(3)
            val randomType = Random.nextInt(3)
            obstacles.add(RunnerObstacle(i, randomLane, startY, randomType))
            val coinLane = (randomLane + 1 + Random.nextInt(2)) % 3
            coins.add(RunnerObstacle(100 + i, coinLane, startY + 80f, 3))
            startY -= Random.nextFloat() * 180f + 160f
        }
        isRunning = true
    }

    LaunchedEffect(isRunning, gameOver) {
        if (!isRunning || gameOver) return@LaunchedEffect
        while (isRunning && !gameOver) {
            delay(33L)
            score += (1 * gameSpeed).toInt()
            gameSpeed = 1.0f + (score / 350f).coerceAtMost(3.8f) // Progressive speed difficulty!

            val speedDelta = 12f * gameSpeed
            val iterator = obstacles.listIterator()
            while (iterator.hasNext()) {
                val obs = iterator.next()
                obs.y += speedDelta

                // Collision Detection with Player
                if (obs.y in 480f..560f && obs.lane == currentLane) {
                    val hit = when (obs.type) {
                        0 -> !isJumping // Low barrier must be jumped
                        1 -> !isSliding // High bridge must be slided
                        else -> true    // Moving Train is lethal
                    }
                    if (hit) {
                        gameOver = true
                        isRunning = false
                        onGameOver(score + (coinsCollected * 10))
                        break
                    }
                }

                if (obs.y > 750f) {
                    obs.y = -200f - Random.nextFloat() * 150f
                    obs.lane = Random.nextInt(3)
                }
            }

            val coinIterator = coins.listIterator()
            while (coinIterator.hasNext()) {
                val c = coinIterator.next()
                c.y += speedDelta
                if (c.y in 480f..560f && c.lane == currentLane) {
                    coinsCollected++
                    c.y = -300f - Random.nextFloat() * 200f
                    c.lane = Random.nextInt(3)
                } else if (c.y > 750f) {
                    c.y = -300f - Random.nextFloat() * 200f
                    c.lane = Random.nextInt(3)
                }
            }
        }
    }

    LaunchedEffect(isJumping) {
        if (isJumping) {
            delay(520L)
            isJumping = false
        }
    }
    LaunchedEffect(isSliding) {
        if (isSliding) {
            delay(520L)
            isSliding = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المسافة: $score م 🏃", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("السرعة: x${String.format("%.1f", gameSpeed)} 🔥", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("العملات: $coinsCollected 🪙", color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val laneWidth = w / 3f

                // Track dividers
                drawLine(Color(0xFF475569), Offset(laneWidth, 0f), Offset(laneWidth, h), 4f)
                drawLine(Color(0xFF475569), Offset(laneWidth * 2f, 0f), Offset(laneWidth * 2f, h), 4f)

                // Moving ties
                val tieOffset = (score * 8) % 60
                for (ty in 0..h.toInt() step 60) {
                    val yPos = ty.toFloat() + tieOffset
                    if (yPos < h) {
                        drawLine(Color(0xFF334155), Offset(10f, yPos), Offset(w - 10f, yPos), 2.5f)
                    }
                }

                // Coins
                coins.forEach { c ->
                    if (c.y in -50f..h + 50f) {
                        val cx = (c.lane * laneWidth) + (laneWidth / 2f)
                        drawCircle(Color(0xFFF59E0B), 14f, Offset(cx, c.y))
                        drawCircle(Color(0xFFFEF08A), 9f, Offset(cx, c.y))
                    }
                }

                // Obstacles
                obstacles.forEach { obs ->
                    if (obs.y in -100f..h + 100f) {
                        val ox = (obs.lane * laneWidth) + 12f
                        val oWidth = laneWidth - 24f
                        when (obs.type) {
                            0 -> {
                                drawRoundRect(Color(0xFFDC2626), Offset(ox, obs.y), Size(oWidth, 36f), CornerRadius(8f, 8f))
                                drawRoundRect(Color(0xFFFACC15), Offset(ox + 8f, obs.y + 6f), Size(oWidth - 16f, 12f), CornerRadius(4f, 4f))
                            }
                            1 -> {
                                drawRoundRect(Color(0xFF2563EB), Offset(ox, obs.y - 40f), Size(oWidth, 42f), CornerRadius(8f, 8f))
                                drawLine(Color(0xFF64748B), Offset(ox, obs.y), Offset(ox, obs.y + 30f), 4f)
                                drawLine(Color(0xFF64748B), Offset(ox + oWidth, obs.y), Offset(ox + oWidth, obs.y + 30f), 4f)
                            }
                            else -> {
                                drawRoundRect(Color(0xFF991B1B), Offset(ox, obs.y - 80f), Size(oWidth, 100f), CornerRadius(14f, 14f))
                                drawCircle(Color(0xFFFEF08A), 8f, Offset(ox + 16f, obs.y + 10f))
                                drawCircle(Color(0xFFFEF08A), 8f, Offset(ox + oWidth - 16f, obs.y + 10f))
                            }
                        }
                    }
                }

                // Player
                val px = (currentLane * laneWidth) + (laneWidth / 2f)
                val py = if (isJumping) 450f else 520f
                val playerRadius = if (isSliding) 18f else 28f
                drawOval(Color(0x66000000), Offset(px - 24f, 550f), Size(48f, 16f))
                drawCircle(Color(0xFF0D9488), playerRadius, Offset(px, py))
                drawCircle(Color(0xFF2DD4BF), playerRadius * 0.7f, Offset(px, py))
            }

            if (!isRunning && !gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏃 لعبة صب واي بيلّو السريعة", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("تفادَ القطارات، واقفز فوق الحواجز، وانزلق بسرعة!", fontSize = 13.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { startNewGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ابدأ الجري السريع 🔥", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            if (gameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDD000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💥 اصطدام! انتهت الجولة", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("المسافة: $score م • العملات: $coinsCollected 🪙", fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { startNewGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("العب مرة تانية 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Virtual Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (isRunning && currentLane > 0) currentLane-- },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "يسار", tint = Color.White)
            }

            Button(
                onClick = { if (isRunning && !isJumping) isJumping = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.size(64.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "قفز", tint = Color.White)
                    Text("قفز", fontSize = 9.sp, color = Color.White)
                }
            }

            Button(
                onClick = { if (isRunning && !isSliding) isSliding = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                modifier = Modifier.size(64.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "انزلاق", tint = Color.White)
                    Text("انزلاق", fontSize = 9.sp, color = Color.White)
                }
            }

            Button(
                onClick = { if (isRunning && currentLane < 2) currentLane++ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "يمين", tint = Color.White)
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 2: FAST MATH & MENTAL CALCULATION (الرياضيات والحساب السريع)
// -------------------------------------------------------------
@Composable
fun FastMathCalculationGame(onGameOver: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isPlaying by remember { mutableStateOf(false) }

    var questionText by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableIntStateOf(0) }
    var options by remember { mutableStateOf(listOf<Int>()) }
    var timeRemainingMs by remember { mutableIntStateOf(4000) }
    var maxTimeMs by remember { mutableIntStateOf(4000) }

    fun generateNewMathQuestion() {
        val opType = Random.nextInt(4) // 0: add, 1: sub, 2: mult, 3: dosage calculation
        val a: Int
        val b: Int
        val ans: Int
        val q: String

        when (opType) {
            0 -> {
                a = Random.nextInt(15, 85)
                b = Random.nextInt(10, 60)
                ans = a + b
                q = "$a + $b = ؟"
            }
            1 -> {
                a = Random.nextInt(40, 100)
                b = Random.nextInt(10, 40)
                ans = a - b
                q = "$a - $b = ؟"
            }
            2 -> {
                a = Random.nextInt(3, 12)
                b = Random.nextInt(3, 12)
                ans = a * b
                q = "$a × $b = ؟"
            }
            else -> {
                val pillsPerDay = Random.nextInt(2, 5)
                val days = Random.nextInt(3, 10)
                ans = pillsPerDay * days
                q = "$pillsPerDay جرعات يومياً لمدة $days أيام = كام جرعة؟"
            }
        }

        correctAnswer = ans
        questionText = q

        // 3 tricky decoy options
        val decoys = mutableSetOf<Int>()
        while (decoys.size < 3) {
            val offset = listOf(-10, -2, -1, 1, 2, 10).random()
            val decoy = ans + offset
            if (decoy != ans && decoy > 0) decoys.add(decoy)
        }
        options = (decoys + ans).shuffled()
        maxTimeMs = (4000 - (score * 40)).coerceAtLeast(1400) // Progressive difficulty!
        timeRemainingMs = maxTimeMs
    }

    LaunchedEffect(isPlaying, timeRemainingMs) {
        if (!isPlaying || lives <= 0) return@LaunchedEffect
        while (isPlaying && timeRemainingMs > 0) {
            delay(50L)
            timeRemainingMs -= 50
        }
        if (timeRemainingMs <= 0 && isPlaying) {
            lives--
            streak = 0
            if (lives <= 0) {
                isPlaying = false
                onGameOver(score)
            } else {
                generateNewMathQuestion()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("النقاط: $score 🔢", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2563EB))
            Text("القلوب: ${"❤️".repeat(lives.coerceAtLeast(0))}", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timer Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
        ) {
            val progress = (timeRemainingMs.toFloat() / maxTimeMs).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(if (progress > 0.3f) Color(0xFF3B82F6) else Color(0xFFEF4444), RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isPlaying) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔢 تحدي الرياضيات والجرعات السريع", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E40AF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("احسب العمليات الحسابية وجرعات الأدوية بسرعة فائقة قبل انتهاء الوقت!", fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            score = 0
                            lives = 3
                            streak = 0
                            isPlaying = true
                            generateNewMathQuestion()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ابدأ تحدي الرياضيات 🔥", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Question Display Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF60A5FA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("المعادلة الرياضية:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = questionText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Option Buttons (2x2 grid)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(options) { _, opt ->
                    Button(
                        onClick = {
                            if (opt == correctAnswer) {
                                score += 15 + (streak * 3)
                                streak++
                                generateNewMathQuestion()
                            } else {
                                lives--
                                streak = 0
                                if (lives <= 0) {
                                    isPlaying = false
                                    onGameOver(score)
                                } else {
                                    generateNewMathQuestion()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(68.dp)
                    ) {
                        Text(
                            text = opt.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 3: HARDCORE MEMORY MATRIX (مراحل عشوائية غير متكررة)
// -------------------------------------------------------------
@Composable
fun HardcoreMemoryMatrixGame(onGameOver: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var isPlaying by remember { mutableStateOf(false) }

    val cards = remember { mutableStateListOf<String>() }
    val revealed = remember { mutableStateListOf<Boolean>() }
    val matched = remember { mutableStateListOf<Boolean>() }
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }

    fun setupNewMatrix() {
        val pool = listOf("💊", "💉", "🧴", "🧪", "🩹", "🩺", "🧬", "🩸", "🔬", "🩻", "🌿", "🍎")
        val pairsCount = 4 + (level.coerceAtMost(4))
        val selected = pool.shuffled().take(pairsCount)
        val deck = (selected + selected).shuffled() // Non-repeating randomized layout

        cards.clear()
        cards.addAll(deck)
        revealed.clear()
        revealed.addAll(List(deck.size) { false })
        matched.clear()
        matched.addAll(List(deck.size) { false })
        firstSelectedIndex = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("المرحلة: $level 🧠", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF7C3AED))
            Text("النقاط: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD97706))
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isPlaying) {
            Button(
                onClick = {
                    score = 0
                    level = 1
                    isPlaying = true
                    setupNewMatrix()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ابدأ اختبار الذاكرة الصعبة 🧠", fontWeight = FontWeight.Bold)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(cards) { index, symbol ->
                    val isFlipped = revealed.getOrElse(index) { false } || matched.getOrElse(index) { false }
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                if (!isFlipped && firstSelectedIndex != index) {
                                    revealed[index] = true
                                    if (firstSelectedIndex == null) {
                                        firstSelectedIndex = index
                                    } else {
                                        val first = firstSelectedIndex!!
                                        if (cards[first] == cards[index]) {
                                            matched[first] = true
                                            matched[index] = true
                                            score += 25
                                            firstSelectedIndex = null
                                            if (matched.all { it }) {
                                                level++
                                                score += 50
                                                setupNewMatrix()
                                            }
                                        } else {
                                            firstSelectedIndex = null
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) Color(0xFFF5F3FF) else Color(0xFF1E293B)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, if (isFlipped) Color(0xFF8B5CF6) else Color(0xFF475569))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (isFlipped) {
                                Text(symbol, fontSize = 28.sp)
                            } else {
                                Text("❓", fontSize = 22.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 4: HYPER SPEED PILL SORTER (فرز الحبوب الناري السريع)
// -------------------------------------------------------------
@Composable
fun HyperPillSorterGame(onGameOver: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isPlaying by remember { mutableStateOf(false) }
    var targetPillColor by remember { mutableStateOf(Color.Red) }
    var targetBinColorName by remember { mutableStateOf("الأحمر") }
    var timeRemainingMs by remember { mutableIntStateOf(1000) }
    var maxTimePerPillMs by remember { mutableIntStateOf(1200) }

    val colors = listOf(
        Triple(Color(0xFFEF4444), "الأحمر", "🔴"),
        Triple(Color(0xFF3B82F6), "الأزرق", "🔵"),
        Triple(Color(0xFF10B981), "الأخضر", "🟢"),
        Triple(Color(0xFFF59E0B), "الأصفر", "🟡")
    )

    fun spawnNextPill() {
        val next = colors.random()
        targetPillColor = next.first
        targetBinColorName = next.second
        maxTimePerPillMs = (1200 - (score * 15)).coerceAtLeast(450)
        timeRemainingMs = maxTimePerPillMs
    }

    LaunchedEffect(isPlaying, timeRemainingMs) {
        if (!isPlaying || lives <= 0) return@LaunchedEffect
        while (isPlaying && timeRemainingMs > 0) {
            delay(50L)
            timeRemainingMs -= 50
        }
        if (timeRemainingMs <= 0 && isPlaying) {
            lives--
            streak = 0
            if (lives <= 0) {
                isPlaying = false
                onGameOver(score)
            } else {
                spawnNextPill()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("النقاط: $score ⚡", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD97706))
            Text("القلوب: ${"❤️".repeat(lives.coerceAtLeast(0))}", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
        ) {
            val progress = (timeRemainingMs.toFloat() / maxTimePerPillMs).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(if (progress > 0.3f) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!isPlaying) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡ لعبة فرز الحبوب الناري", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF92400E))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("السرعة بتزيد مع كل حبة! اضغط على الصندوق المناسب بسرعة قبل ما الوقت يخلص.", fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            score = 0
                            lives = 3
                            streak = 0
                            isPlaying = true
                            spawnNextPill()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ابدأ التحدي السريع 🔥", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(targetPillColor, CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.forEach { (binColor, name, icon) ->
                    Button(
                        onClick = {
                            if (binColor == targetPillColor) {
                                score += 10 + (streak * 2)
                                streak++
                                spawnNextPill()
                            } else {
                                lives--
                                streak = 0
                                if (lives <= 0) {
                                    isPlaying = false
                                    onGameOver(score)
                                } else {
                                    spawnNextPill()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = binColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(icon, fontSize = 22.sp)
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

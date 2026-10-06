package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Medicine
import com.example.ui.screens.AddEditMedicineScreen
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.CertificateVaultScreen
import com.example.ui.screens.DigitalIdCardScreen
import com.example.ui.screens.EmergencyCardScreen
import com.example.ui.screens.FamilyDashboardScreen
import com.example.ui.screens.HealthLogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MiniGamesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PetScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.theme.PilloTheme
import com.example.ui.viewmodel.PilloViewModel

sealed interface AppScreen {
    data object Onboarding : AppScreen
    data object Home : AppScreen
    data class AddEditMedicine(val medicine: Medicine? = null) : AppScreen
    data object EmergencyCard : AppScreen
    data object Settings : AppScreen
    data object HealthLog : AppScreen
    data object CertificateVault : AppScreen
    data object Appointments : AppScreen
    data object FamilyDashboard : AppScreen
    data object MiniGames : AppScreen
    data object Pet : AppScreen
    data object Social : AppScreen
    data object DigitalIdCard : AppScreen
}

class MainActivity : ComponentActivity() {

    private val viewModel: PilloViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val todayDoses by viewModel.todayDoses.collectAsStateWithLifecycle()
            val activeMedicines by viewModel.activeMedicines.collectAsStateWithLifecycle()
            val finishedMedicines by viewModel.finishedMedicines.collectAsStateWithLifecycle()
            val lowStockMedicines by viewModel.lowStockMedicines.collectAsStateWithLifecycle()
            val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
            val certificates by viewModel.certificates.collectAsStateWithLifecycle()
            val healthReadings by viewModel.healthReadings.collectAsStateWithLifecycle()
            val appointments by viewModel.appointments.collectAsStateWithLifecycle()
            val allTodayDoses by viewModel.allTodayDoses.collectAsStateWithLifecycle()

            // Phase 3 States
            val petState by viewModel.petState.collectAsStateWithLifecycle()
            val coinWallet by viewModel.coinWallet.collectAsStateWithLifecycle()
            val unlockedItems by viewModel.unlockedItems.collectAsStateWithLifecycle()
            val gameScores by viewModel.gameScores.collectAsStateWithLifecycle()
            val dailyChallenge by viewModel.dailyChallenge.collectAsStateWithLifecycle()
            val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
            val badges by viewModel.badges.collectAsStateWithLifecycle()
            val challenges by viewModel.challenges.collectAsStateWithLifecycle()
            val publicProfile by viewModel.publicProfile.collectAsStateWithLifecycle()
            val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
            val friends by viewModel.friends.collectAsStateWithLifecycle()
            val caretakerLinks by viewModel.caretakerLinks.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

            // Initial check for onboarding
            LaunchedEffect(userProfile.isOnboarded) {
                if (!userProfile.isOnboarded) {
                    currentScreen = AppScreen.Onboarding
                }
            }

            // Handle direct deep-links from notification
            LaunchedEffect(intent) {
                handleIntent(intent) { screen ->
                    currentScreen = screen
                }
            }

            PilloTheme(ageMode = userProfile.ageMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is AppScreen.Onboarding -> {
                            OnboardingScreen(
                                currentProfile = userProfile,
                                onComplete = { updated ->
                                    viewModel.saveUserProfile(updated) {
                                        currentScreen = AppScreen.Home
                                    }
                                }
                            )
                        }

                        is AppScreen.Home -> {
                            HomeScreen(
                                userProfile = userProfile,
                                todayDoses = todayDoses,
                                activeMedicines = activeMedicines,
                                finishedMedicines = finishedMedicines,
                                lowStockMedicines = lowStockMedicines,
                                onAddMedicine = {
                                    currentScreen = AppScreen.AddEditMedicine(null)
                                },
                                onEditMedicine = { med ->
                                    currentScreen = AppScreen.AddEditMedicine(med)
                                },
                                onTakeDose = { medId, timeMs, label ->
                                    viewModel.markDoseTaken(medId, timeMs, label)
                                },
                                onReplenishMedicine = { medId, amount ->
                                    viewModel.replenishMedicine(medId, amount)
                                },
                                onFinishMedicine = { medId ->
                                    viewModel.finishMedicine(medId)
                                },
                                onCompleteTreatment = { med ->
                                    viewModel.completeTreatment(med)
                                },
                                onExtendTreatment = { med, extraQty ->
                                    viewModel.extendTreatment(med, extraQty)
                                },
                                onSaveProfile = { prof ->
                                    viewModel.saveUserProfile(prof)
                                },
                                onNavigateHealthLog = {
                                    currentScreen = AppScreen.HealthLog
                                },
                                onNavigateCertificates = {
                                    currentScreen = AppScreen.CertificateVault
                                },
                                onNavigateAppointments = {
                                    currentScreen = AppScreen.Appointments
                                },
                                onNavigateFamily = {
                                    currentScreen = AppScreen.FamilyDashboard
                                },
                                onNavigateMiniGames = {
                                    currentScreen = AppScreen.MiniGames
                                },
                                onNavigatePet = {
                                    currentScreen = AppScreen.Pet
                                },
                                onNavigateSocial = {
                                    currentScreen = AppScreen.Social
                                },
                                onNavigateDigitalIdCard = {
                                    currentScreen = AppScreen.DigitalIdCard
                                },
                                onNavigateEmergency = {
                                    currentScreen = AppScreen.EmergencyCard
                                },
                                onNavigateSettings = {
                                    currentScreen = AppScreen.Settings
                                },
                                onTestAlarm = {
                                    viewModel.testAlarm(10)
                                }
                            )
                        }

                        is AppScreen.AddEditMedicine -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            AddEditMedicineScreen(
                                medicineToEdit = screen.medicine,
                                userProfile = userProfile,
                                onSave = { med ->
                                    viewModel.saveMedicine(med) {
                                        currentScreen = AppScreen.Home
                                    }
                                },
                                onDelete = { med ->
                                    viewModel.deleteMedicine(med)
                                    currentScreen = AppScreen.Home
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.HealthLog -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            HealthLogScreen(
                                userProfile = userProfile,
                                activeMedicines = activeMedicines,
                                finishedMedicines = finishedMedicines,
                                readings = healthReadings,
                                onAddReading = { reading ->
                                    viewModel.addHealthReading(reading)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.CertificateVault -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            CertificateVaultScreen(
                                userProfile = userProfile,
                                certificates = certificates,
                                onGenerateSampleCertificate = {
                                    viewModel.generateSampleCertificate()
                                },
                                onDeleteCertificate = { cert ->
                                    viewModel.deleteCertificate(cert)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Appointments -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            AppointmentsScreen(
                                userProfile = userProfile,
                                appointments = appointments,
                                onAddAppointment = { appt ->
                                    viewModel.addAppointment(appt)
                                },
                                onDeleteAppointment = { appt ->
                                    viewModel.deleteAppointment(appt)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.FamilyDashboard -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            FamilyDashboardScreen(
                                profiles = allProfiles,
                                currentProfileId = userProfile.id,
                                allTodayDoses = allTodayDoses,
                                onSelectProfile = { prof ->
                                    viewModel.switchProfile(prof.id)
                                    currentScreen = AppScreen.Home
                                },
                                onAddProfile = { prof ->
                                    viewModel.addProfile(prof)
                                },
                                onDeleteProfile = { prof ->
                                    viewModel.deleteProfile(prof)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.MiniGames -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            MiniGamesScreen(
                                ageMode = userProfile.ageMode,
                                coinWallet = coinWallet,
                                dailyChallenge = dailyChallenge,
                                highScores = gameScores,
                                onCompleteDailyChallenge = {
                                    viewModel.completeDailyChallenge()
                                },
                                onSaveGameScore = { gameId, score ->
                                    viewModel.recordGameScore(gameId, score)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Pet -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            PetScreen(
                                petState = petState,
                                coinWallet = coinWallet,
                                unlockedItems = unlockedItems,
                                ageMode = userProfile.ageMode,
                                onFeedPet = {
                                    viewModel.feedPet()
                                },
                                onChangeSpecies = { species ->
                                    viewModel.changePetSpecies(species)
                                },
                                onEquipItem = { itemId ->
                                    viewModel.equipPetItem(itemId)
                                },
                                onUnlockItem = { itemId, cost, name ->
                                    viewModel.unlockStoreItem(itemId, cost, name)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Social -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            SocialScreen(
                                userProfile = userProfile,
                                publicProfile = publicProfile,
                                challenges = challenges,
                                friends = friends,
                                caretakerLinks = caretakerLinks,
                                certificates = certificates,
                                activeMedicines = activeMedicines,
                                isOnline = isOnline,
                                onCreateAccount = { nick, avatar, minor, consent ->
                                    viewModel.createOrUpdateAccount(nick, avatar, minor, consent)
                                },
                                onUpdateAccountDetails = { name, email, fatherPhone, avatar ->
                                    viewModel.updateUserAccountDetails(name, email, fatherPhone, avatar)
                                },
                                onRewardSon = { sonEmail, coins ->
                                    viewModel.rewardSonWithCoins(amount = coins)
                                },
                                onDeleteAccount = {
                                    viewModel.deleteAccount()
                                },
                                onRegenerateQr = {
                                    viewModel.regenerateQr()
                                },
                                onSendChallenge = { oppId, oppName, type, days ->
                                    viewModel.sendChallenge(oppId, oppName, type, days)
                                },
                                onAcceptChallenge = { ch ->
                                    viewModel.acceptChallenge(ch)
                                },
                                onDeclineChallenge = { ch ->
                                    viewModel.declineChallenge(ch)
                                },
                                onBlockUser = { targetId ->
                                    viewModel.blockUser(targetId)
                                },
                                onInviteCaretaker = { cId, cName ->
                                    viewModel.inviteCaretaker(cId, cName)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.EmergencyCard -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            EmergencyCardScreen(
                                userProfile = userProfile,
                                activeMedicines = activeMedicines,
                                onSaveProfile = { updated ->
                                    viewModel.saveUserProfile(updated)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.DigitalIdCard -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            DigitalIdCardScreen(
                                userProfile = userProfile,
                                certificates = certificates,
                                onSaveProfile = { updated ->
                                    viewModel.saveUserProfile(updated)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Settings -> {
                            BackHandler {
                                currentScreen = AppScreen.Home
                            }
                            SettingsScreen(
                                userProfile = userProfile,
                                onSaveProfile = { updated ->
                                    viewModel.saveUserProfile(updated)
                                },
                                onTestAlarm = { seconds ->
                                    viewModel.testAlarm(seconds)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?, onNavigate: (AppScreen) -> Unit) {
        val target = intent?.getStringExtra("navigate_to")
        if (target == "emergency") {
            onNavigate(AppScreen.EmergencyCard)
        }
    }
}

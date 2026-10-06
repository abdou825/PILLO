package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.model.AgeMode

// Kids Palette (Playful & Bright)
val KidsPrimary = Color(0xFF0284C7)
val KidsOnPrimary = Color.White
val KidsSecondary = Color(0xFFF59E0B)
val KidsTertiary = Color(0xFFEC4899)
val KidsBackground = Color(0xFFF0F9FF)
val KidsSurface = Color.White

// Teens Palette (Dark, Sleek, Neon-touched)
val TeensPrimary = Color(0xFF818CF8)
val TeensOnPrimary = Color(0xFF0F172A)
val TeensSecondary = Color(0xFF34D399)
val TeensBackground = Color(0xFF090D16)
val TeensSurface = Color(0xFF131B2E)
val TeensOnSurface = Color(0xFFF1F5F9)

// Young Adults Palette (Clean, Modern Teal & Mint)
val YoungAdultsPrimary = Color(0xFF0D9488)
val YoungAdultsOnPrimary = Color.White
val YoungAdultsSecondary = Color(0xFF3B82F6)
val YoungAdultsBackground = Color(0xFFF8FAFC)
val YoungAdultsSurface = Color.White

// Adults Palette (Calm Olive, Rich Navy)
val AdultsPrimary = Color(0xFF047857)
val AdultsOnPrimary = Color.White
val AdultsSecondary = Color(0xFF475569)
val AdultsBackground = Color(0xFFF8FAF9)
val AdultsSurface = Color.White

// Seniors Palette (Maximum Contrast & Accessibility)
val SeniorsPrimary = Color(0xFF0038A8)
val SeniorsOnPrimary = Color.White
val SeniorsSecondary = Color(0xFF990000)
val SeniorsBackground = Color(0xFFFFFFFF)
val SeniorsSurface = Color(0xFFF1F5F9)
val SeniorsOnSurface = Color(0xFF000000)

data class AgeModeStyle(
    val ageMode: AgeMode,
    val colorScheme: ColorScheme,
    val bodyFontSizeSp: Int,
    val headlineFontSizeSp: Int,
    val buttonMinHeightDp: Int,
    val cardCornerRadiusDp: Int,
    val isHighContrast: Boolean = false
)

fun getStyleForAgeMode(ageMode: AgeMode): AgeModeStyle {
    return when (ageMode) {
        AgeMode.KIDS -> AgeModeStyle(
            ageMode = ageMode,
            colorScheme = lightColorScheme(
                primary = KidsPrimary,
                onPrimary = KidsOnPrimary,
                secondary = KidsSecondary,
                tertiary = KidsTertiary,
                background = KidsBackground,
                surface = KidsSurface,
                surfaceVariant = Color(0xFFE0F2FE)
            ),
            bodyFontSizeSp = 18,
            headlineFontSizeSp = 24,
            buttonMinHeightDp = 64,
            cardCornerRadiusDp = 24
        )

        AgeMode.TEENS -> AgeModeStyle(
            ageMode = ageMode,
            colorScheme = darkColorScheme(
                primary = TeensPrimary,
                onPrimary = TeensOnPrimary,
                secondary = TeensSecondary,
                background = TeensBackground,
                surface = TeensSurface,
                onSurface = TeensOnSurface,
                surfaceVariant = Color(0xFF1E293B)
            ),
            bodyFontSizeSp = 15,
            headlineFontSizeSp = 22,
            buttonMinHeightDp = 48,
            cardCornerRadiusDp = 16
        )

        AgeMode.YOUNG_ADULTS -> AgeModeStyle(
            ageMode = ageMode,
            colorScheme = lightColorScheme(
                primary = YoungAdultsPrimary,
                onPrimary = YoungAdultsOnPrimary,
                secondary = YoungAdultsSecondary,
                background = YoungAdultsBackground,
                surface = YoungAdultsSurface,
                surfaceVariant = Color(0xFFE2E8F0)
            ),
            bodyFontSizeSp = 16,
            headlineFontSizeSp = 22,
            buttonMinHeightDp = 52,
            cardCornerRadiusDp = 18
        )

        AgeMode.ADULTS -> AgeModeStyle(
            ageMode = ageMode,
            colorScheme = lightColorScheme(
                primary = AdultsPrimary,
                onPrimary = AdultsOnPrimary,
                secondary = AdultsSecondary,
                background = AdultsBackground,
                surface = AdultsSurface,
                surfaceVariant = Color(0xFFE5E7EB)
            ),
            bodyFontSizeSp = 19,
            headlineFontSizeSp = 24,
            buttonMinHeightDp = 56,
            cardCornerRadiusDp = 16
        )

        AgeMode.SENIORS -> AgeModeStyle(
            ageMode = ageMode,
            colorScheme = lightColorScheme(
                primary = SeniorsPrimary,
                onPrimary = SeniorsOnPrimary,
                secondary = SeniorsSecondary,
                background = SeniorsBackground,
                surface = SeniorsSurface,
                onSurface = SeniorsOnSurface,
                surfaceVariant = Color(0xFFE2E8F0),
                outline = Color.Black
            ),
            bodyFontSizeSp = 24, // >= 24sp as strictly specified in Feature 6!
            headlineFontSizeSp = 28,
            buttonMinHeightDp = 76,
            cardCornerRadiusDp = 12,
            isHighContrast = true
        )
    }
}

package com.gymtracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════
// COLOR PALETTE
// ═══════════════════════════════════════════════════════════════
val Crimson = Color(0xFFE94560)
val CrimsonDark = Color(0xFFC73650)
val CrimsonLight = Color(0xFFFF6B81)

val DeepNavy = Color(0xFF0F0F23)
val DarkSurface = Color(0xFF1A1A2E)
val DarkSurfaceVariant = Color(0xFF16213E)
val DarkCard = Color(0xFF1F2544)

val NeonGreen = Color(0xFF00E676)
val NeonBlue = Color(0xFF448AFF)
val NeonOrange = Color(0xFFFF9100)
val NeonPurple = Color(0xFFBB86FC)
val NeonYellow = Color(0xFFFFEB3B)

val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFF44336)

val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0B0B0)
val TextTertiary = Color(0xFF707070)

val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFF0F2F5)

// Muscle map colors
val MuscleUndertrained = Color(0xFF4A4A5A)
val MuscleAdequate = Color(0xFF4CAF50)
val MuscleOvertrained = Color(0xFFF44336)
val MuscleRecovering = Color(0xFFFF9800)

// ═══════════════════════════════════════════════════════════════
// DARK COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = NeonBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGreen,
    onTertiary = Color.Black,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF3A3A5C),
    error = ErrorRed,
    onError = Color.White
)

// ═══════════════════════════════════════════════════════════════
// LIGHT COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    primaryContainer = CrimsonLight,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF1565C0),
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = LightSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFDDDDDD),
    error = ErrorRed,
    onError = Color.White
)

// ═══════════════════════════════════════════════════════════════
// TYPOGRAPHY
// ═══════════════════════════════════════════════════════════════
val GymTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp
    )
)

// ═══════════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════════
@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GymTypography,
        content = content
    )
}

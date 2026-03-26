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
// Psychology: Deep dark = authority/focus  |  Orange = energy/motivation
// Blue-teal  = trust/performance  |  Green = achievement/progress
// ═══════════════════════════════════════════════════════════════

// Primary — Electric Orange (energy, motivation, action — #1 choice for fitness)
val OrangePrimary    = Color(0xFFFF6B35)
val OrangeDark       = Color(0xFFD95A28)
val OrangeLight      = Color(0xFFFF8E5E)
val OrangeContainer  = Color(0xFF3D1A0A)

// Dark backgrounds — deep navy-black (authority, focus, high-performance feel)
val BgDeep           = Color(0xFF0C0C16)   // main background
val SurfaceDark      = Color(0xFF131320)   // cards/surfaces
val SurfaceVariantDk = Color(0xFF1A1A2C)   // elevated surfaces
val CardDark         = Color(0xFF1F1F32)   // cards

// Accent — cool sky-blue (trust, data, stability)
val BlueTrust        = Color(0xFF4FC3F7)
val BlueTrustDark    = Color(0xFF0288D1)

// Tertiary — teal-green (achievement, success, wellness)
val TealSuccess      = Color(0xFF26C6A6)
val TealSuccessDark  = Color(0xFF00897B)

// Semantic
val SuccessGreen     = Color(0xFF26C6A6)
val WarningOrange    = Color(0xFFFFB300)
val ErrorRed         = Color(0xFFFF4757)

// Text
val TextPrimary      = Color(0xFFF0F0F8)
val TextSecondary    = Color(0xFFAAABC0)
val TextTertiary     = Color(0xFF666680)

// Light theme
val LightBackground  = Color(0xFFF5F5FA)
val LightSurface     = Color(0xFFFFFFFF)
val LightCard        = Color(0xFFEEEEF5)

// Muscle map — orange accent palette for dark theme
val MuscleUndertrained  = Color(0xFF3A3A50)
val MuscleAdequate      = Color(0xFF26C6A6)   // teal-green — on track
val MuscleOvertrained   = Color(0xFFFF4757)   // red — too much
val MuscleRecovering    = Color(0xFFFFB300)   // amber — in recovery

// Outline
val OutlineDark      = Color(0xFF2A2A40)
val OutlineLight     = Color(0xFFDDDDE8)

// ═══════════════════════════════════════════════════════════════
// DARK COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(
    primary              = OrangePrimary,
    onPrimary            = Color.White,
    primaryContainer     = OrangeContainer,
    onPrimaryContainer   = OrangeLight,

    secondary            = BlueTrust,
    onSecondary          = Color(0xFF002A3F),
    secondaryContainer   = Color(0xFF00334D),
    onSecondaryContainer = BlueTrust,

    tertiary             = TealSuccess,
    onTertiary           = Color(0xFF00201A),
    tertiaryContainer    = Color(0xFF00352C),
    onTertiaryContainer  = TealSuccess,

    background           = BgDeep,
    onBackground         = TextPrimary,

    surface              = SurfaceDark,
    onSurface            = TextPrimary,
    surfaceVariant       = CardDark,
    onSurfaceVariant     = TextSecondary,

    outline              = OutlineDark,
    outlineVariant       = Color(0xFF1E1E30),

    error                = ErrorRed,
    onError              = Color.White,
    errorContainer       = Color(0xFF3D0A0A),
    onErrorContainer     = ErrorRed
)

// ═══════════════════════════════════════════════════════════════
// LIGHT COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(
    primary              = OrangeDark,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFFFDDD0),
    onPrimaryContainer   = OrangeDark,

    secondary            = BlueTrustDark,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFD0EEFF),
    onSecondaryContainer = BlueTrustDark,

    tertiary             = TealSuccessDark,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFB2F0E8),
    onTertiaryContainer  = TealSuccessDark,

    background           = LightBackground,
    onBackground         = Color(0xFF0E0E1A),

    surface              = LightSurface,
    onSurface            = Color(0xFF0E0E1A),
    surfaceVariant       = LightCard,
    onSurfaceVariant     = Color(0xFF50506A),

    outline              = OutlineLight,
    outlineVariant       = Color(0xFFCCCCDD),

    error                = ErrorRed,
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDDE0),
    onErrorContainer     = Color(0xFF8B0000)
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

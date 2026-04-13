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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════
// SHARED BACKGROUNDS (same across all themes)
// ═══════════════════════════════════════════════════════════════

val BgDeep           = Color(0xFF0C0C16)
val SurfaceDark      = Color(0xFF131320)
val SurfaceVariantDk = Color(0xFF1A1A2C)
val CardDark         = Color(0xFF1F1F32)
val OutlineDark      = Color(0xFF2A2A40)
val OutlineLight     = Color(0xFFDDDDE8)

val LightBackground  = Color(0xFFF5F5FA)
val LightSurface     = Color(0xFFFFFFFF)
val LightCard        = Color(0xFFEEEEF5)

val TextPrimary      = Color(0xFFF0F0F8)
val TextSecondary    = Color(0xFFAAABC0)

val SuccessGreen     = Color(0xFF26C6A6)
val WarningOrange    = Color(0xFFFFB300)
val ErrorRed         = Color(0xFFFF4757)

// Muscle map — same across all themes
val MuscleUndertrained  = Color(0xFF3A3A50)
val MuscleAdequate      = Color(0xFF26C6A6)
val MuscleOvertrained   = Color(0xFFFF4757)
val MuscleRecovering    = Color(0xFFFFB300)

// Legacy names kept so existing screens don't break
val OrangePrimary    = Color(0xFFFF6B35)
val OrangeDark       = Color(0xFFD95A28)
val OrangeLight      = Color(0xFFFF8E5E)
val OrangeContainer  = Color(0xFF3D1A0A)
val BlueTrust        = Color(0xFF4FC3F7)
val BlueTrustDark    = Color(0xFF0288D1)
val TealSuccess      = Color(0xFF26C6A6)
val TealSuccessDark  = Color(0xFF00897B)
val Periwinkle       = Color(0xFF7C83FD)

// ═══════════════════════════════════════════════════════════════
// APP THEME ENUM
// ═══════════════════════════════════════════════════════════════

enum class AppTheme(
    val label: String,
    val darkPrimary: Color,
    val darkPrimaryLight: Color,
    val darkPrimaryContainer: Color,
    val lightPrimary: Color,
    val lightPrimaryContainer: Color,
    val previewColor: Color
) {
    ORANGE(
        label = "Orange",
        darkPrimary          = Color(0xFFFF6B35),
        darkPrimaryLight     = Color(0xFFFF8E5E),
        darkPrimaryContainer = Color(0xFF3D1A0A),
        lightPrimary         = Color(0xFFD95A28),
        lightPrimaryContainer= Color(0xFFFFDDD0),
        previewColor         = Color(0xFFFF6B35)
    ),
    BLUE(
        label = "Blue",
        darkPrimary          = Color(0xFF42A5F5),
        darkPrimaryLight     = Color(0xFF90CAF9),
        darkPrimaryContainer = Color(0xFF0A1E3D),
        lightPrimary         = Color(0xFF1565C0),
        lightPrimaryContainer= Color(0xFFD0E4FF),
        previewColor         = Color(0xFF42A5F5)
    ),
    PURPLE(
        label = "Purple",
        darkPrimary          = Color(0xFFCE93D8),
        darkPrimaryLight     = Color(0xFFE1BEE7),
        darkPrimaryContainer = Color(0xFF2D1040),
        lightPrimary         = Color(0xFF7B1FA2),
        lightPrimaryContainer= Color(0xFFF3E5F5),
        previewColor         = Color(0xFFAB47BC)
    ),
    GREEN(
        label = "Green",
        darkPrimary          = Color(0xFF81C784),
        darkPrimaryLight     = Color(0xFFA5D6A7),
        darkPrimaryContainer = Color(0xFF0A2E10),
        lightPrimary         = Color(0xFF2E7D32),
        lightPrimaryContainer= Color(0xFFDCF5DC),
        previewColor         = Color(0xFF66BB6A)
    ),
    RED(
        label = "Red",
        darkPrimary          = Color(0xFFEF9A9A),
        darkPrimaryLight     = Color(0xFFFFCDD2),
        darkPrimaryContainer = Color(0xFF3D0A0A),
        lightPrimary         = Color(0xFFC62828),
        lightPrimaryContainer= Color(0xFFFFE0E0),
        previewColor         = Color(0xFFEF5350)
    ),
    TEAL(
        label = "Teal",
        darkPrimary          = Color(0xFF4DB6AC),
        darkPrimaryLight     = Color(0xFF80CBC4),
        darkPrimaryContainer = Color(0xFF0A2E2A),
        lightPrimary         = Color(0xFF00695C),
        lightPrimaryContainer= Color(0xFFB2DFDB),
        previewColor         = Color(0xFF26C6A6)
    ),
    GOLD(
        label = "Gold",
        darkPrimary          = Color(0xFFFFD54F),
        darkPrimaryLight     = Color(0xFFFFE082),
        darkPrimaryContainer = Color(0xFF3D2E00),
        lightPrimary         = Color(0xFFF57F17),
        lightPrimaryContainer= Color(0xFFFFF8E1),
        previewColor         = Color(0xFFFFCA28)
    );

    companion object {
        fun fromString(name: String): AppTheme =
            entries.firstOrNull { it.name == name } ?: ORANGE
    }
}

// ═══════════════════════════════════════════════════════════════
// COLOR SCHEME BUILDERS
// ═══════════════════════════════════════════════════════════════

private fun buildDarkScheme(theme: AppTheme): ColorScheme = darkColorScheme(
    primary              = theme.darkPrimary,
    onPrimary            = Color(0xFF0C0C16),
    primaryContainer     = theme.darkPrimaryContainer,
    onPrimaryContainer   = theme.darkPrimaryLight,

    secondary            = Color(0xFF4FC3F7),
    onSecondary          = Color(0xFF002A3F),
    secondaryContainer   = Color(0xFF00334D),
    onSecondaryContainer = Color(0xFF4FC3F7),

    tertiary             = Color(0xFF26C6A6),
    onTertiary           = Color(0xFF00201A),
    tertiaryContainer    = Color(0xFF00352C),
    onTertiaryContainer  = Color(0xFF26C6A6),

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

private fun buildLightScheme(theme: AppTheme): ColorScheme = lightColorScheme(
    primary              = theme.lightPrimary,
    onPrimary            = Color.White,
    primaryContainer     = theme.lightPrimaryContainer,
    onPrimaryContainer   = theme.lightPrimary,

    secondary            = Color(0xFF0288D1),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFD0EEFF),
    onSecondaryContainer = Color(0xFF0288D1),

    tertiary             = Color(0xFF00897B),
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFB2F0E8),
    onTertiaryContainer  = Color(0xFF00897B),

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
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,    fontSize = 36.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 28.sp, letterSpacing = 0.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 24.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp, letterSpacing = 0.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 16.sp, letterSpacing = 0.15.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 13.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, letterSpacing = 0.25.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 10.sp, letterSpacing = 0.5.sp)
)

// ═══════════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════════

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = true,
    appTheme: AppTheme = AppTheme.ORANGE,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) buildDarkScheme(appTheme) else buildLightScheme(appTheme)

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

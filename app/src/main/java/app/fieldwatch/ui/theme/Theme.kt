package app.fieldwatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fieldwatch.R

// BISSA palette (from the BISSA App prototype).
val Gold = Color(0xFFFFAD32)
/** Checked switch / slider / chip fill — the BISSA accent. */
val GoldActive = Color(0xFFFFAD32)
val Amber = Color(0xFFFFAD32)
val SignalRed = Color(0xFFE53E3E)
val BissaBlue = Color(0xFF4497C5)
val BissaGreen = Color(0xFF68C564)
val Night = Color(0xFF020D18)
val Panel = Color(0xFF001220)
val Panel2 = Color(0xFF0A1B2C)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF001828),
    primaryContainer = Color(0xFF2B2108),
    onPrimaryContainer = Gold,
    secondary = BissaBlue,
    onSecondary = Color(0xFF001828),
    tertiary = BissaBlue,
    background = Night,
    onBackground = Color(0xFFE6ECF2),
    surface = Panel,
    onSurface = Color(0xFFE6ECF2),
    surfaceVariant = Panel2,
    onSurfaceVariant = Color(0xFF9AA9B8),
    outline = Color(0xFF1F3347),
    error = SignalRed,
)

/**
 * Red-on-black field display. Background stays dark; chrome and accents
 * are red ramps. Used only while Settings → Night mode is on.
 */
private val NightColors = darkColorScheme(
    primary = Color(0xFFFF5A5A),
    onPrimary = Color(0xFF2A0808),
    primaryContainer = Color(0xFF3A1212),
    onPrimaryContainer = Color(0xFFFF8A8A),
    secondary = Color(0xFFE07070),
    onSecondary = Color(0xFF2A0808),
    tertiary = Color(0xFFCC6666),
    background = Color(0xFF0B0808),
    onBackground = Color(0xFFFFC9C9),
    surface = Color(0xFF161010),
    onSurface = Color(0xFFFFC9C9),
    surfaceVariant = Color(0xFF1E1414),
    onSurfaceVariant = Color(0xFFC48A8A),
    outline = Color(0xFF5A3030),
    error = Color(0xFFFF7A7A),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF9A6400),
    onPrimary = Color.White,
    secondary = Color(0xFF2B6F96),
    tertiary = Color(0xFF2B6F96),
    background = Color(0xFFF2F4F7),
    onBackground = Color(0xFF0A1B2E),
    surface = Color.White,
    onSurface = Color(0xFF0A1B2E),
    surfaceVariant = Color(0xFFE4EAF1),
    onSurfaceVariant = Color(0xFF3F4F60),
    outline = Color(0xFFC3CED9),
    error = Color(0xFFB00020),
)

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private val BissaTypography = Typography().let { base ->
    fun TextStyle.m() = copy(fontFamily = Montserrat)
    Typography(
        displayLarge = base.displayLarge.m(), displayMedium = base.displayMedium.m(),
        displaySmall = base.displaySmall.m(), headlineLarge = base.headlineLarge.m(),
        headlineMedium = base.headlineMedium.m(), headlineSmall = base.headlineSmall.m(),
        titleLarge = base.titleLarge.m(), titleMedium = base.titleMedium.m(),
        titleSmall = base.titleSmall.m().copy(letterSpacing = 0.8.sp),
        bodyLarge = base.bodyLarge.m(), bodyMedium = base.bodyMedium.m(),
        bodySmall = base.bodySmall.m(),
        labelLarge = base.labelLarge.m().copy(letterSpacing = 1.sp),
        labelMedium = base.labelMedium.m().copy(letterSpacing = 1.sp),
        labelSmall = base.labelSmall.m().copy(letterSpacing = 1.2.sp),
    )
}

/** BISSA uses near-square corners (2–4 dp), not Material's pill shapes. */
private val BissaShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(3.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(4.dp),
)

val Mono = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 0.3.sp,
)

@Composable
fun FieldwatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    nightMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = when {
        nightMode -> NightColors
        darkTheme -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(LocalNightMode provides nightMode) {
        MaterialTheme(
            colorScheme = scheme,
            typography = BissaTypography,
            shapes = BissaShapes,
            content = content,
        )
    }
}

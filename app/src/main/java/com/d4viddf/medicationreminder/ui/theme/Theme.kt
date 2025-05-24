package com.d4viddf.medicationreminder.ui.theme
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
// Attempt to import MaterialExpressiveTheme and MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme // Keep for default shapes if needed
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.surfaceColorAtElevation
// Remove Material3.Typography as AppTypography is used
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.d4viddf.medicationreminder.data.ThemeKeys

// New Expressive Color Schemes
private val ExpressiveLightColorScheme = lightColorScheme(
    primary = ExpressiveTeal_Light_Primary,
    onPrimary = ExpressiveTeal_Light_OnPrimary,
    primaryContainer = ExpressiveTeal_Light_PrimaryContainer,
    onPrimaryContainer = ExpressiveTeal_Light_OnPrimaryContainer,
    secondary = ExpressiveOrange_Light_Secondary,
    onSecondary = ExpressiveOrange_Light_OnSecondary,
    secondaryContainer = ExpressiveOrange_Light_SecondaryContainer,
    onSecondaryContainer = ExpressiveOrange_Light_OnSecondaryContainer,
    tertiary = ExpressivePurple_Light_Tertiary,
    onTertiary = ExpressivePurple_Light_OnTertiary,
    tertiaryContainer = ExpressivePurple_Light_TertiaryContainer,
    onTertiaryContainer = ExpressivePurple_Light_OnTertiaryContainer,
    error = ExpressiveLight_Error,
    onError = ExpressiveLight_OnError,
    errorContainer = ExpressiveLight_ErrorContainer,
    onErrorContainer = ExpressiveLight_OnErrorContainer,
    background = ExpressiveLight_Background,
    onBackground = ExpressiveLight_OnBackground,
    surface = ExpressiveLight_Surface,
    onSurface = ExpressiveLight_OnSurface,
    surfaceVariant = ExpressiveLight_SurfaceVariant,
    onSurfaceVariant = ExpressiveLight_OnSurfaceVariant,
    outline = ExpressiveLight_Outline,
    // Optional: Define other colors like inverseSurface, inverseOnSurface, etc.
    // For now, they will use Material 3 defaults based on the main colors.
    scrim = Color.Black, // Default scrim
    inversePrimary = ExpressiveTeal_Dark_Primary // Example inverse
)

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = ExpressiveTeal_Dark_Primary,
    onPrimary = ExpressiveTeal_Dark_OnPrimary,
    primaryContainer = ExpressiveTeal_Dark_PrimaryContainer,
    onPrimaryContainer = ExpressiveTeal_Dark_OnPrimaryContainer,
    secondary = ExpressiveOrange_Dark_Secondary,
    onSecondary = ExpressiveOrange_Dark_OnSecondary,
    secondaryContainer = ExpressiveOrange_Dark_SecondaryContainer,
    onSecondaryContainer = ExpressiveOrange_Dark_OnSecondaryContainer,
    tertiary = ExpressivePurple_Dark_Tertiary,
    onTertiary = ExpressivePurple_Dark_OnTertiary,
    tertiaryContainer = ExpressivePurple_Dark_TertiaryContainer,
    onTertiaryContainer = ExpressivePurple_Dark_OnTertiaryContainer,
    error = ExpressiveDark_Error,
    onError = ExpressiveDark_OnError,
    errorContainer = ExpressiveDark_ErrorContainer,
    onErrorContainer = ExpressiveDark_OnErrorContainer,
    background = ExpressiveDark_Background,
    onBackground = ExpressiveDark_OnBackground,
    surface = ExpressiveDark_Surface,
    onSurface = ExpressiveDark_OnSurface,
    surfaceVariant = ExpressiveDark_SurfaceVariant,
    onSurfaceVariant = ExpressiveDark_OnSurfaceVariant,
    outline = ExpressiveDark_Outline,
    // Optional: Define other colors
    scrim = Color.Black, // Default scrim
    inversePrimary = ExpressiveTeal_Light_Primary // Example inverse
)


// Old schemes (can be removed or kept for reference)
// private val lightScheme = lightColorScheme(...)
// private val darkScheme = darkColorScheme(...)

// Not using contrast color schemes for now
// private val mediumContrastLightColorScheme = ...
// private val highContrastLightColorScheme = ...
// private val mediumContrastDarkColorScheme = ...
// private val highContrastDarkColorScheme = ...

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    themePreference: String = ThemeKeys.SYSTEM,
    dynamicColor: Boolean = false, // Default to false to prioritize static expressive theme
    content: @Composable() () -> Unit
) {
    val useDarkTheme = when (themePreference) {
        ThemeKeys.LIGHT -> false
        ThemeKeys.DARK -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> ExpressiveDarkColorScheme // Use new expressive dark theme
        else -> ExpressiveLightColorScheme // Use new expressive light theme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar transparent for edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            // Set navigation bar to a translucent color for better contrast in edge-to-edge
            window.navigationBarColor = colorScheme.surfaceColorAtElevation(3.dp).toArgb()

            // Ensure system icons (status bar, navigation bar) contrast with content
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    // Attempt to use MaterialExpressiveTheme
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        // Shapes: Using MaterialTheme.shapes for default M3 shapes.
        // If MaterialExpressiveTheme requires its own Shapes object and one isn't available,
        // this might be an issue. Assuming it can take standard M3 Shapes.
        shapes = MaterialTheme.shapes, // Or omit if it takes M3 defaults implicitly
        content = content
    )
}
// Helper to get dp, assuming it's not directly available in this context otherwise
// For surfaceColorAtElevation, dp is needed.
// However, surfaceColorAtElevation is a composable function, so it should be fine.
// If it were a non-composable context, one might need something like:
// import androidx.compose.ui.unit.Dp
// import androidx.compose.ui.platform.LocalDensity
// val elevation = with(LocalDensity.current) { 3.dp.toPx() } -> then convert back or use appropriately.
// But since it's within SideEffect in a Composable, 3.dp should be resolvable.
// Let's ensure the import for Dp is available if needed, or that surfaceColorAtElevation handles it.
// androidx.compose.ui.unit.dp should be available by default with Compose.

package com.example.quotegap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.quotegap.data.model.AppThemeMode
import com.example.quotegap.data.model.QuoteStatus

@Immutable
data class StatusColors(
    val statusText: Color,
    val statusBackground: Color,
    val statusBorder: Color
)

val LocalStatusColors = staticCompositionLocalOf {
    StatusColors(
        statusText = StatusOnBudgetBlueLight,
        statusBackground = StatusOnBudgetBgLight,
        statusBorder = StatusOnBudgetBorderLight
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = SlatePrimaryDark,
    onPrimary = SlateOnPrimaryDark,
    primaryContainer = SlatePrimaryContainerDark,
    onPrimaryContainer = SlateOnPrimaryContainerDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateOnSecondaryDark,
    secondaryContainer = SlateSecondaryContainerDark,
    onSecondaryContainer = SlateOnSecondaryContainerDark,
    tertiary = SlateTertiaryDark,
    onTertiary = SlateOnTertiaryDark,
    tertiaryContainer = SlateTertiaryContainerDark,
    onTertiaryContainer = SlateOnTertiaryContainerDark,
    background = SlateBackgroundDark,
    onBackground = SlateOnBackgroundDark,
    surface = SlateSurfaceDark,
    onSurface = SlateOnSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateOnSurfaceVariantDark,
    outline = SlateOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimaryLight,
    onPrimary = SlateOnPrimaryLight,
    primaryContainer = SlatePrimaryContainerLight,
    onPrimaryContainer = SlateOnPrimaryContainerLight,
    secondary = SlateSecondaryLight,
    onSecondary = SlateOnSecondaryLight,
    secondaryContainer = SlateSecondaryContainerLight,
    onSecondaryContainer = SlateOnSecondaryContainerLight,
    tertiary = SlateTertiaryLight,
    onTertiary = SlateOnTertiaryLight,
    tertiaryContainer = SlateTertiaryContainerLight,
    onTertiaryContainer = SlateOnTertiaryContainerLight,
    background = SlateBackgroundLight,
    onBackground = SlateOnBackgroundLight,
    surface = SlateSurfaceLight,
    onSurface = SlateOnSurfaceLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateOnSurfaceVariantLight,
    outline = SlateOutlineLight
)

@Composable
fun getStatusColors(status: QuoteStatus, isDark: Boolean = isSystemInDarkTheme()): StatusColors {
    return when (status) {
        QuoteStatus.UNDER_BUDGET -> if (isDark) {
            StatusColors(
                statusText = StatusUnderBudgetGreenDark,
                statusBackground = StatusUnderBudgetBgDark,
                statusBorder = StatusUnderBudgetBorderDark
            )
        } else {
            StatusColors(
                statusText = StatusUnderBudgetGreenLight,
                statusBackground = StatusUnderBudgetBgLight,
                statusBorder = StatusUnderBudgetBorderLight
            )
        }

        QuoteStatus.ON_BUDGET -> if (isDark) {
            StatusColors(
                statusText = StatusOnBudgetBlueDark,
                statusBackground = StatusOnBudgetBgDark,
                statusBorder = StatusOnBudgetBorderDark
            )
        } else {
            StatusColors(
                statusText = StatusOnBudgetBlueLight,
                statusBackground = StatusOnBudgetBgLight,
                statusBorder = StatusOnBudgetBorderLight
            )
        }

        QuoteStatus.OVER_BUDGET -> if (isDark) {
            StatusColors(
                statusText = StatusOverBudgetRedDark,
                statusBackground = StatusOverBudgetBgDark,
                statusBorder = StatusOverBudgetBorderDark
            )
        } else {
            StatusColors(
                statusText = StatusOverBudgetRedLight,
                statusBackground = StatusOverBudgetBgLight,
                statusBorder = StatusOverBudgetBorderLight
            )
        }
    }
}

@Composable
fun QuoteGapTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
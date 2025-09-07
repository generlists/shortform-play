package com.sean.ratel.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun RatelappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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
        content = content,
    )
}

@Suppress("ktlint:standard:function-naming")
/**
 * A theme overlay used for dialogs.
 */
@Composable
fun RatelDialogThemeOverlay(content: @Composable () -> Unit) {
    // Rally is always dark themed.
    val dialogColors =
        darkColors(
            primary = Color.White,
            surface = Color.White.copy(alpha = 0.12f).compositeOver(Color.Black),
            onSurface = Color.White,
        )

    // Copy the current [Typography] and replace some text styles for this theme.
    val currentTypography = androidx.compose.material.MaterialTheme.typography
    val dialogTypography =
        currentTypography.copy(
            body2 =
                currentTypography.body1.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    letterSpacing = 1.sp,
                ),
            button =
                currentTypography.button.copy(
                    color = APP_TEXT_COLOR,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.em,
                ),
        )
    androidx.compose.material.MaterialTheme(
        colors = dialogColors,
        typography = dialogTypography,
        content = content,
    )
}

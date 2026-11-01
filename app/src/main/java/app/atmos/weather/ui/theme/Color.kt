package app.atmos.weather.ui.theme

import androidx.compose.ui.graphics.Color
import app.atmos.weather.data.model.ThemePalette

// Primary Atmospheric Palette - Atmos Blue (Default)
val md_theme_light_primary = Color(0xFF1E5BB0)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD6E3FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001B3E)
val md_theme_light_secondary = Color(0xFF3B608F)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD7E3F8)
val md_theme_light_onSecondaryContainer = Color(0xFF001B3C)
val md_theme_light_tertiary = Color(0xFF6750A4)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFE9DDFF)
val md_theme_light_onTertiaryContainer = Color(0xFF22005D)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFF7F9FE)
val md_theme_light_onBackground = Color(0xFF191C20)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_onSurface = Color(0xFF191C20)
val md_theme_light_surfaceVariant = Color(0xFFE1E7F0)
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)
val md_theme_light_outline = Color(0xFF73777F)
val md_theme_light_outlineVariant = Color(0xFFC3C7D0)

val md_theme_dark_primary = Color(0xFFA8C8FF)
val md_theme_dark_onPrimary = Color(0xFF003063)
val md_theme_dark_primaryContainer = Color(0xFF00468C)
val md_theme_dark_onPrimaryContainer = Color(0xFFD6E3FF)
val md_theme_dark_secondary = Color(0xFFA5C8E8)
val md_theme_dark_onSecondary = Color(0xFF053259)
val md_theme_dark_secondaryContainer = Color(0xFF224872)
val md_theme_dark_onSecondaryContainer = Color(0xFFD7E3F8)
val md_theme_dark_tertiary = Color(0xFFCFBCFF)
val md_theme_dark_onTertiary = Color(0xFF381E72)
val md_theme_dark_tertiaryContainer = Color(0xFF4F378A)
val md_theme_dark_onTertiaryContainer = Color(0xFFE9DDFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF0F141C)
val md_theme_dark_onBackground = Color(0xFFE1E2E8)
val md_theme_dark_surface = Color(0xFF161B24)
val md_theme_dark_onSurface = Color(0xFFE1E2E8)
val md_theme_dark_surfaceVariant = Color(0xFF222936)
val md_theme_dark_onSurfaceVariant = Color(0xFFC3C7D0)
val md_theme_dark_outline = Color(0xFF8D919A)
val md_theme_dark_outlineVariant = Color(0xFF43474E)

// Curated Palettes: Primary Accents
fun getPalettePrimary(palette: ThemePalette, isDark: Boolean): Color {
    return when (palette) {
        ThemePalette.ATMOS_BLUE -> if (isDark) Color(0xFFA8C8FF) else Color(0xFF1E5BB0)
        ThemePalette.EMERALD_AURORA -> if (isDark) Color(0xFF81DDB3) else Color(0xFF006C4C)
        ThemePalette.SUNSET_CORAL -> if (isDark) Color(0xFFFFB5A0) else Color(0xFFA03F25)
        ThemePalette.LAVENDER_DUSK -> if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
        ThemePalette.OBSIDIAN_SLATE -> if (isDark) Color(0xFFC4C7C5) else Color(0xFF3B4856)
    }
}

fun getPalettePrimaryContainer(palette: ThemePalette, isDark: Boolean): Color {
    return when (palette) {
        ThemePalette.ATMOS_BLUE -> if (isDark) Color(0xFF00468C) else Color(0xFFD6E3FF)
        ThemePalette.EMERALD_AURORA -> if (isDark) Color(0xFF005138) else Color(0xFF9DF7CE)
        ThemePalette.SUNSET_CORAL -> if (isDark) Color(0xFF7F2811) else Color(0xFFFFDAD2)
        ThemePalette.LAVENDER_DUSK -> if (isDark) Color(0xFF4F378A) else Color(0xFFE9DDFF)
        ThemePalette.OBSIDIAN_SLATE -> if (isDark) Color(0xFF2E3B48) else Color(0xFFD6E4F0)
    }
}

// Weather Atmospheric Tones
val WeatherSunGold = Color(0xFFFDB813)
val WeatherSunGlow = Color(0xFFFFA000)
val WeatherSkyDayLight = Color(0xFF5D9CE6)
val WeatherSkyDayDark = Color(0xFF2B5B9E)
val WeatherSkySunsetLight = Color(0xFFE28B6A)
val WeatherSkySunsetDark = Color(0xFF5D3354)
val WeatherSkyNightLight = Color(0xFF1D263B)
val WeatherSkyNightDark = Color(0xFF0E131F)
val WeatherCloudLight = Color(0xFFFFFFFF)
val WeatherCloudShadow = Color(0xFFD3E0EA)
val WeatherRainDrop = Color(0xFF64B5F6)
val WeatherStormDark = Color(0xFF1A1F2C)
val WeatherLightning = Color(0xFFFFF176)

// AQI Colors
val AqiGood = Color(0xFF4CAF50)
val AqiModerate = Color(0xFFFFB300)
val AqiUnhealthySensitive = Color(0xFFFF7043)
val AqiUnhealthy = Color(0xFFE53935)
val AqiVeryUnhealthy = Color(0xFF8E24AA)
val AqiHazardous = Color(0xFF6D1B28)

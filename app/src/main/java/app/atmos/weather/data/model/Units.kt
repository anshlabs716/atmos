package app.atmos.weather.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun format(celsius: Double): String {
        val value = when (this) {
            CELSIUS -> celsius
            FAHRENHEIT -> (celsius * 9 / 5) + 32
        }
        return "${Math.round(value)}°"
    }

    fun formatWithUnit(celsius: Double): String {
        val value = when (this) {
            CELSIUS -> celsius
            FAHRENHEIT -> (celsius * 9 / 5) + 32
        }
        return "${Math.round(value)}$symbol"
    }
}

@Serializable
enum class WindSpeedUnit(val symbol: String) {
    KMH("km/h"),
    MPH("mph"),
    MS("m/s"),
    KNOTS("kn");

    fun format(kmh: Double): String {
        val converted = when (this) {
            KMH -> kmh
            MPH -> kmh * 0.621371
            MS -> kmh / 3.6
            KNOTS -> kmh * 0.539957
        }
        return "%.1f %s".format(converted, symbol)
    }
}

@Serializable
enum class PressureUnit(val symbol: String) {
    HPA("hPa"),
    INHG("inHg"),
    MMHG("mmHg"),
    MBAR("mbar");

    fun format(hpa: Double): String {
        val converted = when (this) {
            HPA -> hpa
            MBAR -> hpa
            INHG -> hpa * 0.02953
            MMHG -> hpa * 0.750062
        }
        return when (this) {
            INHG -> "%.2f %s".format(converted, symbol)
            else -> "${Math.round(converted)} $symbol"
        }
    }
}

@Serializable
enum class PrecipitationUnit(val symbol: String) {
    MILLIMETERS("mm"),
    INCHES("in");

    fun format(mm: Double): String {
        val value = when (this) {
            MILLIMETERS -> mm
            INCHES -> mm / 25.4
        }
        return "%.1f %s".format(value, symbol)
    }
}

@Serializable
enum class VisibilityUnit(val symbol: String) {
    KILOMETERS("km"),
    MILES("mi");

    fun format(km: Double): String {
        val value = when (this) {
            KILOMETERS -> km
            MILES -> km * 0.621371
        }
        return "%.1f %s".format(value, symbol)
    }
}

@Serializable
enum class TimeFormat {
    H24,
    H12
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
enum class ThemePalette(val label: String) {
    ATMOS_BLUE("Atmos Blue"),
    EMERALD_AURORA("Emerald Aurora"),
    SUNSET_CORAL("Sunset Coral"),
    LAVENDER_DUSK("Lavender Dusk"),
    OBSIDIAN_SLATE("Obsidian Slate")
}

@Serializable
enum class ParallaxIntensity(val label: String, val multiplier: Float) {
    OFF("Off", 0f),
    SUBTLE("Subtle (1x)", 1.0f),
    MEDIUM("Medium (2.5x)", 2.5f),
    BALANCED("Medium (2.5x)", 2.5f),
    STRONG("Strong (4.5x)", 4.5f),
    MAXIMUM("Maximum (7.0x)", 7.0f)
}

@Serializable
enum class UvSensitivity(val label: String, val thresholdOffset: Double) {
    NORMAL("Standard Skin", 0.0),
    SENSITIVE("Sensitive Skin (Early Alerts)", -1.5)
}

@Serializable
enum class AnimationIntensity(val label: String) {
    OFF("Off"),
    GENTLE("Gentle"),
    NORMAL("Normal"),
    EXPRESSIVE("Expressive")
}

@Serializable
enum class CardCornerRadius(val label: String, val radiusDp: Int) {
    EXPRESSIVE("Expressive (28dp)", 28),
    SOFT("Soft (20dp)", 20),
    CRISP("Crisp (12dp)", 12)
}

@Serializable
enum class WeatherIconStyle(val label: String) {
    ROUNDED("Rounded"),
    OUTLINED("Outlined"),
    FILLED("Filled")
}

@Serializable
enum class WeatherProviderType(
    val displayName: String,
    val isFreeNoKey: Boolean,
    val defaultAttribution: String
) {
    OPEN_METEO(
        "Open-Meteo",
        true,
        "Weather data by Open-Meteo.com (CC BY 4.0)"
    ),
    BOM_AUSTRALIA(
        "Bureau of Meteorology (BOM)",
        true,
        "Data provided by Australian Bureau of Meteorology"
    ),
    OPEN_WEATHER_MAP(
        "OpenWeatherMap (API Key)",
        false,
        "Weather data by OpenWeatherMap"
    ),
    WEATHER_API(
        "WeatherAPI (API Key)",
        false,
        "Weather data by WeatherAPI.com"
    )
}

@Serializable
enum class BackgroundRefreshInterval(val label: String, val intervalMinutes: Long) {
    OFF("Disabled", 0),
    HOURS_1("Every 1 Hour", 60),
    HOURS_2("Every 2 Hours", 120),
    HOURS_4("Every 4 Hours", 240),
    HOURS_8("Every 8 Hours", 480)
}

@Serializable
data class HomeScreenSectionConfig(
    val showHourly: Boolean = true,
    val showPrecipitation: Boolean = true,
    val showDaily: Boolean = true,
    val showDetails: Boolean = true,
    val showSunriseSunset: Boolean = true,
    val showMoon: Boolean = true,
    val showAirQuality: Boolean = true
)

@Serializable
data class UserPreferences(
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val speedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val precipUnit: PrecipitationUnit = PrecipitationUnit.MILLIMETERS,
    val visibilityUnit: VisibilityUnit = VisibilityUnit.KILOMETERS,
    val timeFormat: TimeFormat = TimeFormat.H24,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themePalette: ThemePalette = ThemePalette.ATMOS_BLUE,
    val dynamicColor: Boolean = false,
    val animationsEnabled: Boolean = true,
    val animationIntensity: AnimationIntensity = AnimationIntensity.NORMAL,
    val parallaxIntensity: ParallaxIntensity = ParallaxIntensity.MEDIUM,
    val reducedMotion: Boolean = false,
    val cornerRadius: CardCornerRadius = CardCornerRadius.EXPRESSIVE,
    val iconStyle: WeatherIconStyle = WeatherIconStyle.ROUNDED,
    val useGpsLocation: Boolean = false,
    val homeSections: HomeScreenSectionConfig = HomeScreenSectionConfig(),
    val providerType: WeatherProviderType = WeatherProviderType.OPEN_METEO,
    val customApiKey: String = "",
    val refreshInterval: BackgroundRefreshInterval = BackgroundRefreshInterval.HOURS_2,
    val weatherAlertNotifications: Boolean = true,
    val rainNotifications: Boolean = true,
    val dailySummaryNotification: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val uvSensitivity: UvSensitivity = UvSensitivity.NORMAL
)

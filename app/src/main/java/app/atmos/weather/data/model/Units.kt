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
    MMHG("mmHg");

    fun format(hpa: Double): String {
        val converted = when (this) {
            HPA -> hpa
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
data class UserPreferences(
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val speedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val timeFormat: TimeFormat = TimeFormat.H24,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val animationsEnabled: Boolean = true,
    val gyroscopeParallaxEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val useGpsLocation: Boolean = false
)

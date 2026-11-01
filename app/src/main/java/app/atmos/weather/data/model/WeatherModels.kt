package app.atmos.weather.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class WeatherConditionGroup {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

@Serializable
enum class WeatherCondition(
    val title: String,
    val group: WeatherConditionGroup
) {
    CLEAR_SKY("Clear sky", WeatherConditionGroup.CLEAR),
    MAINLY_CLEAR("Mainly clear", WeatherConditionGroup.CLEAR),
    PARTLY_CLOUDY("Partly cloudy", WeatherConditionGroup.PARTLY_CLOUDY),
    OVERCAST("Overcast", WeatherConditionGroup.CLOUDY),
    FOG("Foggy", WeatherConditionGroup.FOG),
    DEPOSITING_RIME_FOG("Freezing fog", WeatherConditionGroup.FOG),
    LIGHT_DRIZZLE("Light drizzle", WeatherConditionGroup.DRIZZLE),
    MODERATE_DRIZZLE("Moderate drizzle", WeatherConditionGroup.DRIZZLE),
    DENSE_DRIZZLE("Dense drizzle", WeatherConditionGroup.DRIZZLE),
    LIGHT_RAIN("Light rain", WeatherConditionGroup.RAIN),
    MODERATE_RAIN("Moderate rain", WeatherConditionGroup.RAIN),
    HEAVY_RAIN("Heavy rain", WeatherConditionGroup.RAIN),
    LIGHT_SNOW("Light snow", WeatherConditionGroup.SNOW),
    MODERATE_SNOW("Moderate snow", WeatherConditionGroup.SNOW),
    HEAVY_SNOW("Heavy snow", WeatherConditionGroup.SNOW),
    THUNDERSTORM("Thunderstorm", WeatherConditionGroup.THUNDERSTORM),
    THUNDERSTORM_HAIL("Thunderstorm with hail", WeatherConditionGroup.THUNDERSTORM);

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition {
            return when (code) {
                0 -> CLEAR_SKY
                1 -> MAINLY_CLEAR
                2 -> PARTLY_CLOUDY
                3 -> OVERCAST
                45, 48 -> FOG
                51, 53 -> LIGHT_DRIZZLE
                55 -> DENSE_DRIZZLE
                56, 57 -> LIGHT_DRIZZLE
                61 -> LIGHT_RAIN
                63 -> MODERATE_RAIN
                65, 66, 67, 80, 81, 82 -> HEAVY_RAIN
                71, 77, 85 -> LIGHT_SNOW
                73 -> MODERATE_SNOW
                75, 86 -> HEAVY_SNOW
                95 -> THUNDERSTORM
                96, 99 -> THUNDERSTORM_HAIL
                else -> PARTLY_CLOUDY
            }
        }
    }
}

@Serializable
data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Double,
    val pressure: Double,
    val uvIndex: Double,
    val visibility: Double,
    val dewPoint: Double,
    val timestamp: Long
)

@Serializable
data class HourlyForecast(
    val time: Long,
    val hourText: String,
    val temperature: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val precipitationProbability: Int,
    val precipitationAmount: Double,
    val isDay: Boolean
)

@Serializable
data class DailyForecast(
    val dateIso: String,
    val dayName: String,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbabilityMax: Int,
    val precipitationSum: Double,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String
)

@Serializable
data class SunriseSunsetData(
    val sunriseTime: String,
    val sunsetTime: String,
    val sunriseEpoch: Long,
    val sunsetEpoch: Long,
    val sunProgress: Float, // 0.0f at sunrise, 1.0f at sunset, or negative/above 1 for night
    val isDay: Boolean,
    val daylightRemainingText: String,
    val daylightDurationText: String
)

@Serializable
data class WeatherOverview(
    val location: LocationItem,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val sunriseSunset: SunriseSunsetData,
    val airQuality: AirQualityData? = null,
    val alerts: List<WeatherAlert> = emptyList(),
    val isCached: Boolean = false,
    val fetchedAtEpochMillis: Long = System.currentTimeMillis()
)

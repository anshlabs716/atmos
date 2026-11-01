package app.atmos.weather.data.model

import androidx.compose.ui.graphics.Color
import app.atmos.weather.ui.theme.AqiGood
import app.atmos.weather.ui.theme.AqiHazardous
import app.atmos.weather.ui.theme.AqiModerate
import app.atmos.weather.ui.theme.AqiUnhealthy
import app.atmos.weather.ui.theme.AqiUnhealthySensitive
import app.atmos.weather.ui.theme.AqiVeryUnhealthy
import kotlinx.serialization.Serializable

@Serializable
enum class AqiCategory(val label: String, val description: String) {
    GOOD("Good", "Air quality is satisfactory and poses little or no risk."),
    MODERATE("Moderate", "Air quality is acceptable. Sensitive individuals may experience mild effects."),
    UNHEALTHY_SENSITIVE("Unhealthy for Sensitive Groups", "General public not likely affected. Sensitive individuals may experience discomfort."),
    UNHEALTHY("Unhealthy", "Everyone may begin to experience health effects. Limit prolonged outdoor exertion."),
    VERY_UNHEALTHY("Very Unhealthy", "Health alert: The risk of health effects is increased for everyone."),
    HAZARDOUS("Hazardous", "Health warnings of emergency conditions. Entire population is more likely to be affected.");

    val color: Color
        get() = when (this) {
            GOOD -> AqiGood
            MODERATE -> AqiModerate
            UNHEALTHY_SENSITIVE -> AqiUnhealthySensitive
            UNHEALTHY -> AqiUnhealthy
            VERY_UNHEALTHY -> AqiVeryUnhealthy
            HAZARDOUS -> AqiHazardous
        }

    companion object {
        fun fromAqi(aqi: Int): AqiCategory {
            return when {
                aqi <= 50 -> GOOD
                aqi <= 100 -> MODERATE
                aqi <= 150 -> UNHEALTHY_SENSITIVE
                aqi <= 200 -> UNHEALTHY
                aqi <= 300 -> VERY_UNHEALTHY
                else -> HAZARDOUS
            }
        }
    }
}

@Serializable
data class AirQualityData(
    val aqi: Int,
    val category: AqiCategory,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double? = null,
    val nitrogenDioxide: Double? = null
)

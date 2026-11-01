package app.atmos.weather.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class AlertSeverity(val displayName: String) {
    ADVISORY("Advisory"),
    WATCH("Watch"),
    WARNING("Warning"),
    CRITICAL("Severe Warning");

    val color: Color
        get() = when (this) {
            ADVISORY -> Color(0xFFFBC02D)
            WATCH -> Color(0xFFFFA000)
            WARNING -> Color(0xFFF57C00)
            CRITICAL -> Color(0xFFD32F2F)
        }
}

@Serializable
data class WeatherAlert(
    val id: String,
    val title: String,
    val severity: AlertSeverity,
    val description: String,
    val source: String,
    val effectiveTime: String,
    val expiresTime: String
)

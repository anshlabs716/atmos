package app.atmos.weather.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MoonPhase(
    val title: String,
    val description: String
) {
    NEW_MOON("New Moon", "The Moon is between Earth and the Sun; the illuminated side faces away from Earth."),
    WAXING_CRESCENT("Waxing Crescent", "A thin sliver of light appears on the right edge as the Moon grows."),
    FIRST_QUARTER("First Quarter", "Half of the Moon's face is illuminated as it reaches one-quarter of its orbit."),
    WAXING_GIBBOUS("Waxing Gibbous", "More than half of the Moon is illuminated and growing toward full."),
    FULL_MOON("Full Moon", "The Moon is opposite the Sun; its fully illuminated hemisphere is visible from Earth."),
    WANING_GIBBOUS("Waning Gibbous", "More than half is illuminated but decreasing after the Full Moon."),
    LAST_QUARTER("Last Quarter", "The left half of the Moon is illuminated as it completes three-quarters of its orbit."),
    WANING_CRESCENT("Waning Crescent", "A thin crescent remains on the left edge before becoming a New Moon.")
}

@Serializable
data class MoonData(
    val phase: MoonPhase,
    val illuminationPercent: Int,
    val moonriseTime: String,
    val moonsetTime: String,
    val nextEventName: String,
    val nextEventDays: Int,
    val ageDays: Double,
    val timezone: String,
    val dateText: String,
    val isMoonAboveHorizon: Boolean = false
)

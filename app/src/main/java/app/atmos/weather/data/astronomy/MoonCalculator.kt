package app.atmos.weather.data.astronomy

import app.atmos.weather.data.model.MoonData
import app.atmos.weather.data.model.MoonPhase
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

object MoonCalculator {

    // Reference epoch: Known astronomical new moon on Jan 6, 2000 at 18:14 UTC
    private const val SYNODIC_MONTH = 29.530588853
    private const val NEW_MOON_REFERENCE_EPOCH_MS = 947182440000L

    fun calculateMoonData(
        epochMillis: Long = System.currentTimeMillis(),
        timezoneId: String = "auto"
    ): MoonData {
        val zoneId = runCatching {
            if (timezoneId.isBlank() || timezoneId.equals("auto", ignoreCase = true)) {
                ZoneId.systemDefault()
            } else {
                ZoneId.of(timezoneId)
            }
        }.getOrDefault(ZoneId.systemDefault())

        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)
        val dateText = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

        // Calculate days since reference new moon
        val diffMs = epochMillis - NEW_MOON_REFERENCE_EPOCH_MS
        val daysSinceEpoch = diffMs.toDouble() / (1000.0 * 60.0 * 60.0 * 24.0)
        var ageDays = daysSinceEpoch % SYNODIC_MONTH
        if (ageDays < 0) ageDays += SYNODIC_MONTH

        // Illumination percentage from phase angle
        val phaseAngle = (ageDays / SYNODIC_MONTH) * 2.0 * PI
        val illuminationFraction = (1.0 - cos(phaseAngle)) / 2.0
        val illuminationPercent = (illuminationFraction * 100.0).roundToInt().coerceIn(0, 100)

        // Moon phase categorization
        val phase = when {
            ageDays < 1.0 || ageDays >= 28.5 -> MoonPhase.NEW_MOON
            ageDays < 6.8 -> MoonPhase.WAXING_CRESCENT
            ageDays < 8.2 -> MoonPhase.FIRST_QUARTER
            ageDays < 13.8 -> MoonPhase.WAXING_GIBBOUS
            ageDays < 15.8 -> MoonPhase.FULL_MOON
            ageDays < 21.8 -> MoonPhase.WANING_GIBBOUS
            ageDays < 23.2 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }

        // Next major event
        val (nextEventName, nextEventDays) = if (ageDays < 14.76) {
            val daysUntilFull = (14.76 - ageDays).roundToInt().coerceAtLeast(1)
            "Full Moon" to daysUntilFull
        } else {
            val daysUntilNew = (SYNODIC_MONTH - ageDays).roundToInt().coerceAtLeast(1)
            "New Moon" to daysUntilNew
        }

        // Approximate local moonrise and moonset based on local day and solar offset
        // At New Moon (age 0), moon rises near ~06:00 and sets ~18:00
        // Daily progression shifts rise/set by ~50.5 minutes later each day
        val totalMinutesPerDay = 24 * 60
        val baseRiseMinutes = (6 * 60 + (ageDays * 50.49)).toInt() % totalMinutesPerDay
        val baseSetMinutes = (baseRiseMinutes + (12 * 60 + 25)) % totalMinutesPerDay

        val riseH = baseRiseMinutes / 60
        val riseM = baseRiseMinutes % 60
        val setH = baseSetMinutes / 60
        val setM = baseSetMinutes % 60

        val moonriseTime = "%02d:%02d".format(riseH, riseM)
        val moonsetTime = "%02d:%02d".format(setH, setM)

        val currentLocalMinutes = zonedDateTime.hour * 60 + zonedDateTime.minute
        val isAbove = if (baseSetMinutes > baseRiseMinutes) {
            currentLocalMinutes in baseRiseMinutes..baseSetMinutes
        } else {
            currentLocalMinutes >= baseRiseMinutes || currentLocalMinutes <= baseSetMinutes
        }

        return MoonData(
            phase = phase,
            illuminationPercent = illuminationPercent,
            moonriseTime = moonriseTime,
            moonsetTime = moonsetTime,
            nextEventName = nextEventName,
            nextEventDays = nextEventDays,
            ageDays = ageDays,
            timezone = zoneId.id,
            dateText = dateText,
            isMoonAboveHorizon = isAbove
        )
    }
}

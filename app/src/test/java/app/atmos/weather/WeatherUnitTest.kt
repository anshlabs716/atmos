package app.atmos.weather

import app.atmos.weather.data.model.AqiCategory
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.WeatherCondition
import app.atmos.weather.data.model.WeatherConditionGroup
import app.atmos.weather.data.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUnitTest {

    @Test
    fun testWmoCodeMapping() {
        assertEquals(WeatherCondition.CLEAR_SKY, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherConditionGroup.CLEAR, WeatherCondition.fromWmoCode(0).group)

        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(2))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromWmoCode(3))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherCondition.LIGHT_RAIN, WeatherCondition.fromWmoCode(61))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
    }

    @Test
    fun testTemperatureUnitConversion() {
        val tempCelsius = 20.0
        assertEquals("20°", TemperatureUnit.CELSIUS.format(tempCelsius))
        assertEquals("20°C", TemperatureUnit.CELSIUS.formatWithUnit(tempCelsius))

        // 20 C = 68 F
        assertEquals("68°", TemperatureUnit.FAHRENHEIT.format(tempCelsius))
        assertEquals("68°F", TemperatureUnit.FAHRENHEIT.formatWithUnit(tempCelsius))

        val freezing = 0.0
        assertEquals("0°", TemperatureUnit.CELSIUS.format(freezing))
        assertEquals("32°", TemperatureUnit.FAHRENHEIT.format(freezing))
    }

    @Test
    fun testWindSpeedConversion() {
        val speedKmh = 36.0
        assertTrue(WindSpeedUnit.KMH.format(speedKmh).contains("36.0 km/h"))
        assertTrue(WindSpeedUnit.MS.format(speedKmh).contains("10.0 m/s"))
        assertTrue(WindSpeedUnit.MPH.format(speedKmh).contains("mph"))
    }

    @Test
    fun testPressureUnitConversion() {
        val hpa = 1013.25
        assertEquals("1013 hPa", PressureUnit.HPA.format(hpa))
        assertTrue(PressureUnit.INHG.format(hpa).contains("inHg"))
    }

    @Test
    fun testAqiClassification() {
        assertEquals(AqiCategory.GOOD, AqiCategory.fromAqi(30))
        assertEquals(AqiCategory.MODERATE, AqiCategory.fromAqi(75))
        assertEquals(AqiCategory.UNHEALTHY_SENSITIVE, AqiCategory.fromAqi(120))
        assertEquals(AqiCategory.UNHEALTHY, AqiCategory.fromAqi(175))
        assertEquals(AqiCategory.VERY_UNHEALTHY, AqiCategory.fromAqi(250))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.fromAqi(350))
    }
}

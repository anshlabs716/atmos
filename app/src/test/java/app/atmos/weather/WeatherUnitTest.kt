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
        assertEquals("1013 mbar", PressureUnit.MBAR.format(hpa))
        assertTrue(PressureUnit.INHG.format(hpa).contains("inHg"))
        assertTrue(PressureUnit.MMHG.format(hpa).contains("mmHg"))
    }

    @Test
    fun testPrecipitationUnitConversion() {
        val mm = 25.4
        assertTrue(app.atmos.weather.data.model.PrecipitationUnit.MILLIMETERS.format(mm).contains("25.4 mm"))
        assertTrue(app.atmos.weather.data.model.PrecipitationUnit.INCHES.format(mm).contains("1.0 in"))
    }

    @Test
    fun testVisibilityUnitConversion() {
        val km = 10.0
        assertTrue(app.atmos.weather.data.model.VisibilityUnit.KILOMETERS.format(km).contains("10.0 km"))
        assertTrue(app.atmos.weather.data.model.VisibilityUnit.MILES.format(km).contains("6.2 mi"))
    }

    @Test
    fun testKnotsWindSpeed() {
        val speedKmh = 18.52
        assertTrue(WindSpeedUnit.KNOTS.format(speedKmh).contains("kn"))
    }

    @Test
    fun testParallaxIntensityMultipliers() {
        assertEquals(0.0f, app.atmos.weather.data.model.ParallaxIntensity.OFF.multiplier, 0.001f)
        assertEquals(1.0f, app.atmos.weather.data.model.ParallaxIntensity.SUBTLE.multiplier, 0.001f)
        assertEquals(2.2f, app.atmos.weather.data.model.ParallaxIntensity.BALANCED.multiplier, 0.001f)
        assertEquals(4.5f, app.atmos.weather.data.model.ParallaxIntensity.STRONG.multiplier, 0.001f)
    }

    @Test
    fun testHomeScreenSectionConfigDefaults() {
        val config = app.atmos.weather.data.model.HomeScreenSectionConfig()
        assertTrue(config.showHourly)
        assertTrue(config.showPrecipitation)
        assertTrue(config.showDaily)
        assertTrue(config.showDetails)
        assertTrue(config.showSunriseSunset)
        assertTrue(config.showAirQuality)
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

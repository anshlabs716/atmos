package app.atmos.weather.data.provider

import app.atmos.weather.data.model.AirQualityData
import app.atmos.weather.data.model.AqiCategory
import app.atmos.weather.data.model.CurrentWeather
import app.atmos.weather.data.model.DailyForecast
import app.atmos.weather.data.model.HourlyForecast
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.SunriseSunsetData
import app.atmos.weather.data.model.WeatherAlert
import app.atmos.weather.data.model.WeatherCondition
import app.atmos.weather.data.model.WeatherOverview
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Serializable
internal data class OpenMeteoForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val current: OpenMeteoCurrent? = null,
    val hourly: OpenMeteoHourly? = null,
    val daily: OpenMeteoDaily? = null
)

@Serializable
internal data class OpenMeteoCurrent(
    val time: String,
    @SerialName("temperature_2m") val temperature2m: Double = 0.0,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Double = 50.0,
    @SerialName("apparent_temperature") val apparentTemperature: Double = 0.0,
    @SerialName("is_day") val isDay: Int = 1,
    val precipitation: Double = 0.0,
    @SerialName("weather_code") val weatherCode: Int = 0,
    @SerialName("surface_pressure") val surfacePressure: Double = 1013.25,
    @SerialName("wind_speed_10m") val windSpeed10m: Double = 0.0,
    @SerialName("wind_direction_10m") val windDirection10m: Double = 0.0,
    @SerialName("uv_index") val uvIndex: Double = 0.0
)

@Serializable
internal data class OpenMeteoHourly(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m") val temperature2m: List<Double?> = emptyList(),
    @SerialName("relative_humidity_2m") val relativeHumidity2m: List<Double?> = emptyList(),
    @SerialName("dew_point_2m") val dewPoint2m: List<Double?> = emptyList(),
    @SerialName("apparent_temperature") val apparentTemperature: List<Double?> = emptyList(),
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
    val precipitation: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("surface_pressure") val surfacePressure: List<Double?> = emptyList(),
    val visibility: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed10m: List<Double?> = emptyList(),
    @SerialName("uv_index") val uvIndex: List<Double?> = emptyList(),
    @SerialName("is_day") val isDay: List<Int?> = emptyList()
)

@Serializable
internal data class OpenMeteoDaily(
    val time: List<String> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max") val temperature2mMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val temperature2mMin: List<Double?> = emptyList(),
    val sunrise: List<String?> = emptyList(),
    val sunset: List<String?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    @SerialName("precipitation_sum") val precipitationSum: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeed10mMax: List<Double?> = emptyList()
)

@Serializable
internal data class OpenMeteoAirQualityResponse(
    val current: OpenMeteoAirQualityCurrent? = null
)

@Serializable
internal data class OpenMeteoAirQualityCurrent(
    val time: String? = null,
    @SerialName("european_aqi") val europeanAqi: Int? = null,
    @SerialName("us_aqi") val usAqi: Int? = null,
    val pm10: Double? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val ozone: Double? = null,
    @SerialName("nitrogen_dioxide") val nitrogenDioxide: Double? = null
)

@Serializable
internal data class OpenMeteoGeocodingResponse(
    val results: List<OpenMeteoGeocodingResult>? = null
)

@Serializable
internal data class OpenMeteoGeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val timezone: String? = null
)

class OpenMeteoProvider(
    private val client: HttpClient = createDefaultHttpClient()
) : WeatherProvider {

    override val providerName: String = "Open-Meteo"
    override val attributionText: String = "Weather data by Open-Meteo.com (CC BY 4.0)"
    override val attributionUrl: String = "https://open-meteo.com/"

    companion object {
        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 15000
                    connectTimeoutMillis = 10000
                    socketTimeoutMillis = 15000
                }
            }
        }
    }

    override suspend fun getFullWeather(location: LocationItem): Result<WeatherOverview> =
        withContext(Dispatchers.IO) {
            try {
                val forecastResp: OpenMeteoForecastResponse = client.get("https://api.open-meteo.com/v1/forecast") {
                    parameter("latitude", location.latitude)
                    parameter("longitude", location.longitude)
                    parameter(
                        "current",
                        "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,uv_index"
                    )
                    parameter(
                        "hourly",
                        "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,surface_pressure,visibility,wind_speed_10m,uv_index,is_day"
                    )
                    parameter(
                        "daily",
                        "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max"
                    )
                    parameter("timezone", "auto")
                }.body()

                val rawCurrent = forecastResp.current ?: throw IllegalStateException("Current weather data unavailable")

                // Extract Dew point & visibility from nearest hourly
                val dewPoint = forecastResp.hourly?.dewPoint2m?.firstOrNull() ?: 10.0
                val visibility = (forecastResp.hourly?.visibility?.firstOrNull() ?: 10000.0) / 1000.0 // km

                val currentWeather = CurrentWeather(
                    temperature = rawCurrent.temperature2m,
                    apparentTemperature = rawCurrent.apparentTemperature,
                    weatherCode = rawCurrent.weatherCode,
                    condition = WeatherCondition.fromWmoCode(rawCurrent.weatherCode),
                    isDay = rawCurrent.isDay == 1,
                    humidity = rawCurrent.relativeHumidity2m.toInt(),
                    windSpeed = rawCurrent.windSpeed10m,
                    windDirection = rawCurrent.windDirection10m,
                    pressure = rawCurrent.surfacePressure,
                    uvIndex = rawCurrent.uvIndex,
                    visibility = visibility,
                    dewPoint = dewPoint,
                    timestamp = System.currentTimeMillis()
                )

                // Parse Hourly Forecasts (up to next 24 hours)
                val hourlyList = mutableListOf<HourlyForecast>()
                val hourlyTimes = forecastResp.hourly?.time.orEmpty()
                val hourlyTemps = forecastResp.hourly?.temperature2m.orEmpty()
                val hourlyCodes = forecastResp.hourly?.weatherCode.orEmpty()
                val hourlyProbs = forecastResp.hourly?.precipitationProbability.orEmpty()
                val hourlyPrecip = forecastResp.hourly?.precipitation.orEmpty()
                val hourlyIsDay = forecastResp.hourly?.isDay.orEmpty()

                val nowEpoch = System.currentTimeMillis() / 1000
                var addedCount = 0

                for (i in hourlyTimes.indices) {
                    if (addedCount >= 24) break
                    val timeString = hourlyTimes[i]
                    // ISO format: "2026-11-01T12:00"
                    val parsedTime = try {
                        LocalDateTime.parse(timeString).atZone(ZoneId.systemDefault()).toEpochSecond()
                    } catch (e: Exception) {
                        nowEpoch + (i * 3600)
                    }

                    // Only take items from recent hour onwards
                    if (parsedTime >= nowEpoch - 3600) {
                        val hourText = try {
                            val dt = LocalDateTime.parse(timeString)
                            dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                        } catch (e: Exception) {
                            "${i % 24}:00"
                        }

                        val temp = hourlyTemps.getOrNull(i) ?: 15.0
                        val code = hourlyCodes.getOrNull(i) ?: 0
                        val prob = hourlyProbs.getOrNull(i) ?: 0
                        val amount = hourlyPrecip.getOrNull(i) ?: 0.0
                        val isDay = (hourlyIsDay.getOrNull(i) ?: 1) == 1

                        hourlyList.add(
                            HourlyForecast(
                                time = parsedTime,
                                hourText = if (addedCount == 0) "Now" else hourText,
                                temperature = temp,
                                weatherCode = code,
                                condition = WeatherCondition.fromWmoCode(code),
                                precipitationProbability = prob,
                                precipitationAmount = amount,
                                isDay = isDay
                            )
                        )
                        addedCount++
                    }
                }

                // Parse Daily Forecasts (up to 7 days)
                val dailyList = mutableListOf<DailyForecast>()
                val dailyTimes = forecastResp.daily?.time.orEmpty()
                val dailyCodes = forecastResp.daily?.weatherCode.orEmpty()
                val dailyMaxTemps = forecastResp.daily?.temperature2mMax.orEmpty()
                val dailyMinTemps = forecastResp.daily?.temperature2mMin.orEmpty()
                val dailySunrises = forecastResp.daily?.sunrise.orEmpty()
                val dailySunsets = forecastResp.daily?.sunset.orEmpty()
                val dailyUvMax = forecastResp.daily?.uvIndexMax.orEmpty()
                val dailyPrecipSum = forecastResp.daily?.precipitationSum.orEmpty()
                val dailyPrecipProb = forecastResp.daily?.precipitationProbabilityMax.orEmpty()

                for (i in dailyTimes.indices) {
                    if (i >= 7) break
                    val dateIso = dailyTimes[i]
                    val dayName = try {
                        val ld = LocalDate.parse(dateIso)
                        if (i == 0) "Today" else ld.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                    } catch (e: Exception) {
                        if (i == 0) "Today" else "Day $i"
                    }

                    val code = dailyCodes.getOrNull(i) ?: 0
                    val maxT = dailyMaxTemps.getOrNull(i) ?: 20.0
                    val minT = dailyMinTemps.getOrNull(i) ?: 10.0
                    val sunrise = dailySunrises.getOrNull(i).orEmpty()
                    val sunset = dailySunsets.getOrNull(i).orEmpty()
                    val uv = dailyUvMax.getOrNull(i) ?: 3.0
                    val precipSum = dailyPrecipSum.getOrNull(i) ?: 0.0
                    val precipProb = dailyPrecipProb.getOrNull(i) ?: 0

                    dailyList.add(
                        DailyForecast(
                            dateIso = dateIso,
                            dayName = dayName,
                            weatherCode = code,
                            condition = WeatherCondition.fromWmoCode(code),
                            tempMax = maxT,
                            tempMin = minT,
                            precipitationProbabilityMax = precipProb,
                            precipitationSum = precipSum,
                            uvIndexMax = uv,
                            sunrise = sunrise,
                            sunset = sunset
                        )
                    )
                }

                // Calculate Sunrise and Sunset Data
                val todaySunriseStr = dailySunrises.firstOrNull().orEmpty()
                val todaySunsetStr = dailySunsets.firstOrNull().orEmpty()

                val (sunriseEpoch, sunsetEpoch, sunriseFormatted, sunsetFormatted) = parseSunriseSunsetTimes(
                    todaySunriseStr,
                    todaySunsetStr
                )

                val sunProgress = calculateSunProgress(nowEpoch, sunriseEpoch, sunsetEpoch)
                val isDaytime = nowEpoch in sunriseEpoch..sunsetEpoch

                val daylightDurationSeconds = max(0L, sunsetEpoch - sunriseEpoch)
                val hoursTotal = daylightDurationSeconds / 3600
                val minsTotal = (daylightDurationSeconds % 3600) / 60
                val durationText = "${hoursTotal}h ${minsTotal}m of daylight"

                val remainingText = if (nowEpoch < sunriseEpoch) {
                    val waitSecs = sunriseEpoch - nowEpoch
                    "Sunrise in ${waitSecs / 3600}h ${(waitSecs % 3600) / 60}m"
                } else if (nowEpoch in sunriseEpoch..sunsetEpoch) {
                    val remSecs = sunsetEpoch - nowEpoch
                    "${remSecs / 3600}h ${(remSecs % 3600) / 60}m until sunset"
                } else {
                    "Sun has set"
                }

                val sunriseSunsetData = SunriseSunsetData(
                    sunriseTime = sunriseFormatted,
                    sunsetTime = sunsetFormatted,
                    sunriseEpoch = sunriseEpoch,
                    sunsetEpoch = sunsetEpoch,
                    sunProgress = sunProgress,
                    isDay = isDaytime,
                    daylightRemainingText = remainingText,
                    daylightDurationText = durationText
                )

                // Fetch Air Quality in parallel / gracefully
                val airQuality = getAirQuality(location.latitude, location.longitude).getOrNull()

                Result.success(
                    WeatherOverview(
                        location = location,
                        current = currentWeather,
                        hourly = hourlyList,
                        daily = dailyList,
                        sunriseSunset = sunriseSunsetData,
                        airQuality = airQuality,
                        alerts = emptyList(),
                        isCached = false,
                        fetchedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseSunriseSunsetTimes(
        sunriseIso: String,
        sunsetIso: String
    ): Quadruple<Long, Long, String, String> {
        val now = Instant.now().epochSecond
        var sunriseEpoch = now - 14400
        var sunsetEpoch = now + 14400
        var sunriseFormatted = "06:30"
        var sunsetFormatted = "18:45"

        try {
            if (sunriseIso.isNotBlank()) {
                val dt = LocalDateTime.parse(sunriseIso)
                sunriseEpoch = dt.atZone(ZoneId.systemDefault()).toEpochSecond()
                sunriseFormatted = dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            }
            if (sunsetIso.isNotBlank()) {
                val dt = LocalDateTime.parse(sunsetIso)
                sunsetEpoch = dt.atZone(ZoneId.systemDefault()).toEpochSecond()
                sunsetFormatted = dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            }
        } catch (_: Exception) {}

        return Quadruple(sunriseEpoch, sunsetEpoch, sunriseFormatted, sunsetFormatted)
    }

    private fun calculateSunProgress(now: Long, sunrise: Long, sunset: Long): Float {
        if (sunset <= sunrise) return 0.5f
        if (now < sunrise) {
            val totalNight = 12 * 3600
            val diff = sunrise - now
            return (-diff.toFloat() / totalNight).coerceIn(-1f, 0f)
        }
        if (now > sunset) {
            val totalNight = 12 * 3600
            val diff = now - sunset
            return (1f + (diff.toFloat() / totalNight)).coerceIn(1f, 2f)
        }
        return ((now - sunrise).toFloat() / (sunset - sunrise).toFloat()).coerceIn(0f, 1f)
    }

    override suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityData> =
        withContext(Dispatchers.IO) {
            try {
                val resp: OpenMeteoAirQualityResponse =
                    client.get("https://air-quality-api.open-meteo.com/v1/air-quality") {
                        parameter("latitude", latitude)
                        parameter("longitude", longitude)
                        parameter("current", "european_aqi,us_aqi,pm10,pm2_5,ozone,nitrogen_dioxide")
                        parameter("timezone", "auto")
                    }.body()

                val cur = resp.current ?: throw IllegalStateException("Air quality data not available")
                val aqi = cur.usAqi ?: cur.europeanAqi ?: 35
                val category = AqiCategory.fromAqi(aqi)

                Result.success(
                    AirQualityData(
                        aqi = aqi,
                        category = category,
                        pm25 = cur.pm25 ?: 8.5,
                        pm10 = cur.pm10 ?: 14.0,
                        ozone = cur.ozone,
                        nitrogenDioxide = cur.nitrogenDioxide
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun searchLocations(query: String): Result<List<LocationItem>> =
        withContext(Dispatchers.IO) {
            if (query.trim().length < 2) return@withContext Result.success(emptyList())
            try {
                val resp: OpenMeteoGeocodingResponse =
                    client.get("https://geocoding-api.open-meteo.com/v1/search") {
                        parameter("name", query.trim())
                        parameter("count", 10)
                        parameter("language", "en")
                        parameter("format", "json")
                    }.body()

                val list = resp.results.orEmpty().map { res ->
                    LocationItem(
                        id = "loc_${res.id}",
                        name = res.name,
                        country = res.country.orEmpty(),
                        admin1 = res.admin1,
                        latitude = res.latitude,
                        longitude = res.longitude,
                        timezone = res.timezone ?: "auto"
                    )
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getWeatherAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>> {
        // Open-Meteo free tier does not have a global public active alerts endpoint,
        // so we return empty list or fallback to severe condition-derived alerts
        return Result.success(emptyList())
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

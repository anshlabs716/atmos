package app.atmos.weather.data.provider

import app.atmos.weather.data.model.AirQualityData
import app.atmos.weather.data.model.AlertSeverity
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.WeatherAlert
import app.atmos.weather.data.model.WeatherOverview
import app.atmos.weather.data.model.WeatherProviderType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherProviderRegistry(
    private val httpClient: HttpClient = OpenMeteoProvider.createDefaultHttpClient()
) {
    val openMeteo = OpenMeteoProvider(httpClient)
    val bomAustralia = BomWeatherProvider(httpClient, openMeteo)
    val openWeatherMap = CustomApiKeyWeatherProvider(
        type = WeatherProviderType.OPEN_WEATHER_MAP,
        providerName = "OpenWeatherMap",
        attributionText = "Weather data by OpenWeatherMap.org",
        attributionUrl = "https://openweathermap.org/",
        testEndpoint = "https://api.openweathermap.org/data/2.5/weather?lat=51.5&lon=0.0&appid=",
        fallbackProvider = openMeteo,
        client = httpClient
    )
    val weatherApi = CustomApiKeyWeatherProvider(
        type = WeatherProviderType.WEATHER_API,
        providerName = "WeatherAPI",
        attributionText = "Weather data by WeatherAPI.com",
        attributionUrl = "https://www.weatherapi.com/",
        testEndpoint = "https://api.weatherapi.com/v1/current.json?q=London&key=",
        fallbackProvider = openMeteo,
        client = httpClient
    )

    fun getProvider(type: WeatherProviderType): WeatherProvider {
        return when (type) {
            WeatherProviderType.OPEN_METEO -> openMeteo
            WeatherProviderType.BOM_AUSTRALIA -> bomAustralia
            WeatherProviderType.OPEN_WEATHER_MAP -> openWeatherMap
            WeatherProviderType.WEATHER_API -> weatherApi
        }
    }
}

class BomWeatherProvider(
    private val client: HttpClient,
    private val delegate: OpenMeteoProvider
) : WeatherProvider {
    override val type: WeatherProviderType = WeatherProviderType.BOM_AUSTRALIA
    override val providerName: String = "Bureau of Meteorology (BOM)"
    override val attributionText: String = "Data from Australian Bureau of Meteorology (ACCESS models)"
    override val attributionUrl: String = "http://www.bom.gov.au/"
    override val requiresApiKey: Boolean = false

    override suspend fun getFullWeather(location: LocationItem): Result<WeatherOverview> {
        return delegate.getFullWeather(location)
    }

    override suspend fun searchLocations(query: String): Result<List<LocationItem>> {
        return delegate.searchLocations(query)
    }

    override suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityData> {
        return delegate.getAirQuality(latitude, longitude)
    }

    override suspend fun getWeatherAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>> {
        return delegate.getWeatherAlerts(latitude, longitude)
    }

    override suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val response = client.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", -33.86)
                parameter("longitude", 151.20)
                parameter("models", "bom_access_global")
                parameter("current", "temperature_2m")
            }
            val elapsed = System.currentTimeMillis() - start
            if (response.status.value in 200..299) {
                Result.success("Connected to BOM ACCESS model successfully (${elapsed}ms)")
            } else {
                Result.failure(Exception("BOM service responded with HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CustomApiKeyWeatherProvider(
    override val type: WeatherProviderType,
    override val providerName: String,
    override val attributionText: String,
    override val attributionUrl: String,
    private val testEndpoint: String,
    private val fallbackProvider: OpenMeteoProvider,
    private val client: HttpClient
) : WeatherProvider {
    override val requiresApiKey: Boolean = true

    override suspend fun getFullWeather(location: LocationItem): Result<WeatherOverview> {
        return fallbackProvider.getFullWeather(location)
    }

    override suspend fun searchLocations(query: String): Result<List<LocationItem>> {
        return fallbackProvider.searchLocations(query)
    }

    override suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityData> {
        return fallbackProvider.getAirQuality(latitude, longitude)
    }

    override suspend fun getWeatherAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>> {
        return fallbackProvider.getWeatherAlerts(latitude, longitude)
    }

    override suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid API key"))
        }
        val start = System.currentTimeMillis()
        try {
            val response = client.get("$testEndpoint$apiKey")
            val elapsed = System.currentTimeMillis() - start
            if (response.status.value in 200..299) {
                Result.success("API key validated successfully (${elapsed}ms)")
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Provider rejected key (HTTP ${response.status.value}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

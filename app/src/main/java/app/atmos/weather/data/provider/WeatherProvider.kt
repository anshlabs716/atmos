package app.atmos.weather.data.provider

import app.atmos.weather.data.model.AirQualityData
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.WeatherAlert
import app.atmos.weather.data.model.WeatherOverview
import app.atmos.weather.data.model.WeatherProviderType

interface WeatherProvider {
    val type: WeatherProviderType
    val providerName: String
    val attributionText: String
    val attributionUrl: String
    val requiresApiKey: Boolean

    suspend fun getFullWeather(location: LocationItem): Result<WeatherOverview>
    suspend fun searchLocations(query: String): Result<List<LocationItem>>
    suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityData>
    suspend fun getWeatherAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>>
    suspend fun testConnection(apiKey: String = ""): Result<String>
}

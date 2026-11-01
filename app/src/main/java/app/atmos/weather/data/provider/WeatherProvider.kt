package app.atmos.weather.data.provider

import app.atmos.weather.data.model.AirQualityData
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.WeatherAlert
import app.atmos.weather.data.model.WeatherOverview

interface WeatherProvider {
    val providerName: String
    val attributionText: String
    val attributionUrl: String

    suspend fun getFullWeather(location: LocationItem): Result<WeatherOverview>
    suspend fun searchLocations(query: String): Result<List<LocationItem>>
    suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityData>
    suspend fun getWeatherAlerts(latitude: Double, longitude: Double): Result<List<WeatherAlert>>
}

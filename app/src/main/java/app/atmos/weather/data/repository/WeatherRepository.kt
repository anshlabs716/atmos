package app.atmos.weather.data.repository

import app.atmos.weather.data.local.PreferencesManager
import app.atmos.weather.data.local.WeatherCache
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.UserPreferences
import app.atmos.weather.data.model.WeatherOverview
import app.atmos.weather.data.provider.OpenMeteoProvider
import app.atmos.weather.data.provider.WeatherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WeatherRepository(
    private val preferencesManager: PreferencesManager,
    private val weatherCache: WeatherCache,
    val provider: WeatherProvider = OpenMeteoProvider()
) {
    val userPreferencesFlow: Flow<UserPreferences> = preferencesManager.userPreferencesFlow
    val savedLocationsFlow: Flow<List<LocationItem>> = preferencesManager.savedLocationsFlow
    val selectedLocationIdFlow: Flow<String> = preferencesManager.selectedLocationIdFlow

    suspend fun getSelectedLocation(): LocationItem {
        val locations = savedLocationsFlow.first()
        val selectedId = selectedLocationIdFlow.first()
        return locations.find { it.id == selectedId } ?: locations.first()
    }

    suspend fun setSelectedLocation(locationId: String) {
        preferencesManager.setSelectedLocationId(locationId)
    }

    suspend fun addLocation(location: LocationItem) {
        val current = savedLocationsFlow.first().toMutableList()
        if (current.none { it.id == location.id || (it.name == location.name && it.country == location.country) }) {
            current.add(location)
            preferencesManager.saveLocations(current)
        }
        preferencesManager.setSelectedLocationId(location.id)
    }

    suspend fun removeLocation(locationId: String) {
        val current = savedLocationsFlow.first().toMutableList()
        if (current.size > 1) {
            current.removeAll { it.id == locationId }
            preferencesManager.saveLocations(current)
            val selectedId = selectedLocationIdFlow.first()
            if (selectedId == locationId) {
                preferencesManager.setSelectedLocationId(current.first().id)
            }
        }
    }

    suspend fun fetchWeather(location: LocationItem, forceRefresh: Boolean = false): Result<WeatherOverview> {
        val cached = weatherCache.getWeather(location.id)
        if (!forceRefresh && cached != null) {
            val cacheAge = System.currentTimeMillis() - cached.fetchedAtEpochMillis
            // Cache valid for 20 minutes if not forced
            if (cacheAge < 20 * 60 * 1000) {
                return Result.success(cached)
            }
        }

        val networkResult = provider.getFullWeather(location)
        return if (networkResult.isSuccess) {
            val fresh = networkResult.getOrThrow().copy(isCached = false)
            weatherCache.saveWeather(fresh)
            Result.success(fresh)
        } else {
            if (cached != null) {
                // Return cached data marked as cached
                Result.success(cached.copy(isCached = true))
            } else {
                networkResult
            }
        }
    }

    suspend fun searchCities(query: String): Result<List<LocationItem>> {
        return provider.searchLocations(query)
    }

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        preferencesManager.updatePreferences(transform)
    }
}

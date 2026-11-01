package app.atmos.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.atmos.weather.data.model.DefaultLocations
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.data.model.TimeFormat
import app.atmos.weather.data.model.UserPreferences
import app.atmos.weather.data.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "atmos_settings")

class PreferencesManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        val KEY_SPEED_UNIT = stringPreferencesKey("speed_unit")
        val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val KEY_GYROSCOPE = booleanPreferencesKey("gyroscope_parallax")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_USE_GPS = booleanPreferencesKey("use_gps_location")
        val KEY_SAVED_LOCATIONS = stringPreferencesKey("saved_locations_json")
        val KEY_SELECTED_LOCATION_ID = stringPreferencesKey("selected_location_id")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            tempUnit = runCatching {
                TemperatureUnit.valueOf(prefs[KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name)
            }.getOrDefault(TemperatureUnit.CELSIUS),
            speedUnit = runCatching {
                WindSpeedUnit.valueOf(prefs[KEY_SPEED_UNIT] ?: WindSpeedUnit.KMH.name)
            }.getOrDefault(WindSpeedUnit.KMH),
            pressureUnit = runCatching {
                PressureUnit.valueOf(prefs[KEY_PRESSURE_UNIT] ?: PressureUnit.HPA.name)
            }.getOrDefault(PressureUnit.HPA),
            timeFormat = runCatching {
                TimeFormat.valueOf(prefs[KEY_TIME_FORMAT] ?: TimeFormat.H24.name)
            }.getOrDefault(TimeFormat.H24),
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
            animationsEnabled = prefs[KEY_ANIMATIONS] ?: true,
            gyroscopeParallaxEnabled = prefs[KEY_GYROSCOPE] ?: true,
            reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
            useGpsLocation = prefs[KEY_USE_GPS] ?: false
        )
    }

    val savedLocationsFlow: Flow<List<LocationItem>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[KEY_SAVED_LOCATIONS]
        if (rawJson.isNullOrBlank()) {
            DefaultLocations
        } else {
            runCatching {
                json.decodeFromString<List<LocationItem>>(rawJson)
            }.getOrDefault(DefaultLocations)
        }
    }

    val selectedLocationIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_LOCATION_ID] ?: DefaultLocations.first().id
    }

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        context.dataStore.edit { prefs ->
            val current = UserPreferences(
                tempUnit = runCatching {
                    TemperatureUnit.valueOf(prefs[KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name)
                }.getOrDefault(TemperatureUnit.CELSIUS),
                speedUnit = runCatching {
                    WindSpeedUnit.valueOf(prefs[KEY_SPEED_UNIT] ?: WindSpeedUnit.KMH.name)
                }.getOrDefault(WindSpeedUnit.KMH),
                pressureUnit = runCatching {
                    PressureUnit.valueOf(prefs[KEY_PRESSURE_UNIT] ?: PressureUnit.HPA.name)
                }.getOrDefault(PressureUnit.HPA),
                timeFormat = runCatching {
                    TimeFormat.valueOf(prefs[KEY_TIME_FORMAT] ?: TimeFormat.H24.name)
                }.getOrDefault(TimeFormat.H24),
                themeMode = runCatching {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                }.getOrDefault(ThemeMode.SYSTEM),
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
                animationsEnabled = prefs[KEY_ANIMATIONS] ?: true,
                gyroscopeParallaxEnabled = prefs[KEY_GYROSCOPE] ?: true,
                reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
                useGpsLocation = prefs[KEY_USE_GPS] ?: false
            )
            val updated = transform(current)
            prefs[KEY_TEMP_UNIT] = updated.tempUnit.name
            prefs[KEY_SPEED_UNIT] = updated.speedUnit.name
            prefs[KEY_PRESSURE_UNIT] = updated.pressureUnit.name
            prefs[KEY_TIME_FORMAT] = updated.timeFormat.name
            prefs[KEY_THEME_MODE] = updated.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = updated.dynamicColor
            prefs[KEY_ANIMATIONS] = updated.animationsEnabled
            prefs[KEY_GYROSCOPE] = updated.gyroscopeParallaxEnabled
            prefs[KEY_REDUCED_MOTION] = updated.reducedMotion
            prefs[KEY_USE_GPS] = updated.useGpsLocation
        }
    }

    suspend fun saveLocations(locations: List<LocationItem>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_LOCATIONS] = json.encodeToString(locations)
        }
    }

    suspend fun setSelectedLocationId(locationId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_LOCATION_ID] = locationId
        }
    }
}

package app.atmos.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.atmos.weather.data.model.AnimationIntensity
import app.atmos.weather.data.model.BackgroundRefreshInterval
import app.atmos.weather.data.model.CardCornerRadius
import app.atmos.weather.data.model.DefaultLocations
import app.atmos.weather.data.model.HomeScreenSectionConfig
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.ParallaxIntensity
import app.atmos.weather.data.model.PrecipitationUnit
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.data.model.ThemePalette
import app.atmos.weather.data.model.TimeFormat
import app.atmos.weather.data.model.UserPreferences
import app.atmos.weather.data.model.VisibilityUnit
import app.atmos.weather.data.model.WeatherIconStyle
import app.atmos.weather.data.model.WeatherProviderType
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
        val KEY_PRECIP_UNIT = stringPreferencesKey("precip_unit")
        val KEY_VISIBILITY_UNIT = stringPreferencesKey("visibility_unit")
        val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_THEME_PALETTE = stringPreferencesKey("theme_palette")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val KEY_ANIMATION_INTENSITY = stringPreferencesKey("animation_intensity")
        val KEY_PARALLAX_INTENSITY = stringPreferencesKey("parallax_intensity")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_CORNER_RADIUS = stringPreferencesKey("corner_radius")
        val KEY_ICON_STYLE = stringPreferencesKey("icon_style")
        val KEY_USE_GPS = booleanPreferencesKey("use_gps_location")
        val KEY_HOME_SECTIONS = stringPreferencesKey("home_sections_json")
        val KEY_PROVIDER_TYPE = stringPreferencesKey("provider_type")
        val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val KEY_REFRESH_INTERVAL = stringPreferencesKey("refresh_interval")
        val KEY_ALERT_NOTIFICATIONS = booleanPreferencesKey("alert_notifications")
        val KEY_RAIN_NOTIFICATIONS = booleanPreferencesKey("rain_notifications")
        val KEY_DAILY_SUMMARY_NOTIFICATION = booleanPreferencesKey("daily_summary_notification")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_UV_SENSITIVITY = stringPreferencesKey("uv_sensitivity")
        val KEY_SAVED_LOCATIONS = stringPreferencesKey("saved_locations_json")
        val KEY_SELECTED_LOCATION_ID = stringPreferencesKey("selected_location_id")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val homeSections = prefs[KEY_HOME_SECTIONS]?.let { raw ->
            runCatching { json.decodeFromString<HomeScreenSectionConfig>(raw) }.getOrNull()
        } ?: HomeScreenSectionConfig()

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
            precipUnit = runCatching {
                PrecipitationUnit.valueOf(prefs[KEY_PRECIP_UNIT] ?: PrecipitationUnit.MILLIMETERS.name)
            }.getOrDefault(PrecipitationUnit.MILLIMETERS),
            visibilityUnit = runCatching {
                VisibilityUnit.valueOf(prefs[KEY_VISIBILITY_UNIT] ?: VisibilityUnit.KILOMETERS.name)
            }.getOrDefault(VisibilityUnit.KILOMETERS),
            timeFormat = runCatching {
                TimeFormat.valueOf(prefs[KEY_TIME_FORMAT] ?: TimeFormat.H24.name)
            }.getOrDefault(TimeFormat.H24),
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            themePalette = runCatching {
                ThemePalette.valueOf(prefs[KEY_THEME_PALETTE] ?: ThemePalette.ATMOS_BLUE.name)
            }.getOrDefault(ThemePalette.ATMOS_BLUE),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
            animationsEnabled = prefs[KEY_ANIMATIONS] ?: true,
            animationIntensity = runCatching {
                AnimationIntensity.valueOf(prefs[KEY_ANIMATION_INTENSITY] ?: AnimationIntensity.NORMAL.name)
            }.getOrDefault(AnimationIntensity.NORMAL),
            parallaxIntensity = runCatching {
                ParallaxIntensity.valueOf(prefs[KEY_PARALLAX_INTENSITY] ?: ParallaxIntensity.BALANCED.name)
            }.getOrDefault(ParallaxIntensity.BALANCED),
            reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
            cornerRadius = runCatching {
                CardCornerRadius.valueOf(prefs[KEY_CORNER_RADIUS] ?: CardCornerRadius.EXPRESSIVE.name)
            }.getOrDefault(CardCornerRadius.EXPRESSIVE),
            iconStyle = runCatching {
                WeatherIconStyle.valueOf(prefs[KEY_ICON_STYLE] ?: WeatherIconStyle.ROUNDED.name)
            }.getOrDefault(WeatherIconStyle.ROUNDED),
            useGpsLocation = prefs[KEY_USE_GPS] ?: false,
            homeSections = homeSections,
            providerType = runCatching {
                WeatherProviderType.valueOf(prefs[KEY_PROVIDER_TYPE] ?: WeatherProviderType.OPEN_METEO.name)
            }.getOrDefault(WeatherProviderType.OPEN_METEO),
            customApiKey = prefs[KEY_CUSTOM_API_KEY] ?: "",
            refreshInterval = runCatching {
                BackgroundRefreshInterval.valueOf(prefs[KEY_REFRESH_INTERVAL] ?: BackgroundRefreshInterval.HOURS_2.name)
            }.getOrDefault(BackgroundRefreshInterval.HOURS_2),
            weatherAlertNotifications = prefs[KEY_ALERT_NOTIFICATIONS] ?: true,
            rainNotifications = prefs[KEY_RAIN_NOTIFICATIONS] ?: true,
            dailySummaryNotification = prefs[KEY_DAILY_SUMMARY_NOTIFICATION] ?: false,
            hapticFeedbackEnabled = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
            uvSensitivity = runCatching {
                app.atmos.weather.data.model.UvSensitivity.valueOf(prefs[KEY_UV_SENSITIVITY] ?: app.atmos.weather.data.model.UvSensitivity.NORMAL.name)
            }.getOrDefault(app.atmos.weather.data.model.UvSensitivity.NORMAL)
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
            val homeSections = prefs[KEY_HOME_SECTIONS]?.let { raw ->
                runCatching { json.decodeFromString<HomeScreenSectionConfig>(raw) }.getOrNull()
            } ?: HomeScreenSectionConfig()

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
                precipUnit = runCatching {
                    PrecipitationUnit.valueOf(prefs[KEY_PRECIP_UNIT] ?: PrecipitationUnit.MILLIMETERS.name)
                }.getOrDefault(PrecipitationUnit.MILLIMETERS),
                visibilityUnit = runCatching {
                    VisibilityUnit.valueOf(prefs[KEY_VISIBILITY_UNIT] ?: VisibilityUnit.KILOMETERS.name)
                }.getOrDefault(VisibilityUnit.KILOMETERS),
                timeFormat = runCatching {
                    TimeFormat.valueOf(prefs[KEY_TIME_FORMAT] ?: TimeFormat.H24.name)
                }.getOrDefault(TimeFormat.H24),
                themeMode = runCatching {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                }.getOrDefault(ThemeMode.SYSTEM),
                themePalette = runCatching {
                    ThemePalette.valueOf(prefs[KEY_THEME_PALETTE] ?: ThemePalette.ATMOS_BLUE.name)
                }.getOrDefault(ThemePalette.ATMOS_BLUE),
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
                animationsEnabled = prefs[KEY_ANIMATIONS] ?: true,
                animationIntensity = runCatching {
                    AnimationIntensity.valueOf(prefs[KEY_ANIMATION_INTENSITY] ?: AnimationIntensity.NORMAL.name)
                }.getOrDefault(AnimationIntensity.NORMAL),
                parallaxIntensity = runCatching {
                    ParallaxIntensity.valueOf(prefs[KEY_PARALLAX_INTENSITY] ?: ParallaxIntensity.BALANCED.name)
                }.getOrDefault(ParallaxIntensity.BALANCED),
                reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
                cornerRadius = runCatching {
                    CardCornerRadius.valueOf(prefs[KEY_CORNER_RADIUS] ?: CardCornerRadius.EXPRESSIVE.name)
                }.getOrDefault(CardCornerRadius.EXPRESSIVE),
                iconStyle = runCatching {
                    WeatherIconStyle.valueOf(prefs[KEY_ICON_STYLE] ?: WeatherIconStyle.ROUNDED.name)
                }.getOrDefault(WeatherIconStyle.ROUNDED),
                useGpsLocation = prefs[KEY_USE_GPS] ?: false,
                homeSections = homeSections,
                providerType = runCatching {
                    WeatherProviderType.valueOf(prefs[KEY_PROVIDER_TYPE] ?: WeatherProviderType.OPEN_METEO.name)
                }.getOrDefault(WeatherProviderType.OPEN_METEO),
                customApiKey = prefs[KEY_CUSTOM_API_KEY] ?: "",
                refreshInterval = runCatching {
                    BackgroundRefreshInterval.valueOf(prefs[KEY_REFRESH_INTERVAL] ?: BackgroundRefreshInterval.HOURS_2.name)
                }.getOrDefault(BackgroundRefreshInterval.HOURS_2),
                weatherAlertNotifications = prefs[KEY_ALERT_NOTIFICATIONS] ?: true,
                rainNotifications = prefs[KEY_RAIN_NOTIFICATIONS] ?: true,
                dailySummaryNotification = prefs[KEY_DAILY_SUMMARY_NOTIFICATION] ?: false,
                hapticFeedbackEnabled = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
                uvSensitivity = runCatching {
                    app.atmos.weather.data.model.UvSensitivity.valueOf(prefs[KEY_UV_SENSITIVITY] ?: app.atmos.weather.data.model.UvSensitivity.NORMAL.name)
                }.getOrDefault(app.atmos.weather.data.model.UvSensitivity.NORMAL)
            )
            val updated = transform(current)
            prefs[KEY_TEMP_UNIT] = updated.tempUnit.name
            prefs[KEY_SPEED_UNIT] = updated.speedUnit.name
            prefs[KEY_PRESSURE_UNIT] = updated.pressureUnit.name
            prefs[KEY_PRECIP_UNIT] = updated.precipUnit.name
            prefs[KEY_VISIBILITY_UNIT] = updated.visibilityUnit.name
            prefs[KEY_TIME_FORMAT] = updated.timeFormat.name
            prefs[KEY_THEME_MODE] = updated.themeMode.name
            prefs[KEY_THEME_PALETTE] = updated.themePalette.name
            prefs[KEY_DYNAMIC_COLOR] = updated.dynamicColor
            prefs[KEY_ANIMATIONS] = updated.animationsEnabled
            prefs[KEY_ANIMATION_INTENSITY] = updated.animationIntensity.name
            prefs[KEY_PARALLAX_INTENSITY] = updated.parallaxIntensity.name
            prefs[KEY_REDUCED_MOTION] = updated.reducedMotion
            prefs[KEY_CORNER_RADIUS] = updated.cornerRadius.name
            prefs[KEY_ICON_STYLE] = updated.iconStyle.name
            prefs[KEY_USE_GPS] = updated.useGpsLocation
            prefs[KEY_HOME_SECTIONS] = json.encodeToString(updated.homeSections)
            prefs[KEY_PROVIDER_TYPE] = updated.providerType.name
            prefs[KEY_CUSTOM_API_KEY] = updated.customApiKey
            prefs[KEY_REFRESH_INTERVAL] = updated.refreshInterval.name
            prefs[KEY_ALERT_NOTIFICATIONS] = updated.weatherAlertNotifications
            prefs[KEY_RAIN_NOTIFICATIONS] = updated.rainNotifications
            prefs[KEY_DAILY_SUMMARY_NOTIFICATION] = updated.dailySummaryNotification
            prefs[KEY_HAPTIC_FEEDBACK] = updated.hapticFeedbackEnabled
            prefs[KEY_UV_SENSITIVITY] = updated.uvSensitivity.name
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

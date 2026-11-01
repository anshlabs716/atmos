package app.atmos.weather.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.AnimationIntensity
import app.atmos.weather.data.model.BackgroundRefreshInterval
import app.atmos.weather.data.model.CardCornerRadius
import app.atmos.weather.data.model.HomeScreenSectionConfig
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
import app.atmos.weather.data.repository.WeatherRepository
import app.atmos.weather.workers.WeatherSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _connectionTestResult = MutableStateFlow<String?>(null)
    val connectionTestResult: StateFlow<String?> = _connectionTestResult.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun setTempUnit(unit: TemperatureUnit) {
        viewModelScope.launch { repository.updatePreferences { it.copy(tempUnit = unit) } }
    }

    fun setSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch { repository.updatePreferences { it.copy(speedUnit = unit) } }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch { repository.updatePreferences { it.copy(pressureUnit = unit) } }
    }

    fun setPrecipUnit(unit: PrecipitationUnit) {
        viewModelScope.launch { repository.updatePreferences { it.copy(precipUnit = unit) } }
    }

    fun setVisibilityUnit(unit: VisibilityUnit) {
        viewModelScope.launch { repository.updatePreferences { it.copy(visibilityUnit = unit) } }
    }

    fun setTimeFormat(format: TimeFormat) {
        viewModelScope.launch { repository.updatePreferences { it.copy(timeFormat = format) } }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updatePreferences { it.copy(themeMode = mode) } }
    }

    fun setThemePalette(palette: ThemePalette) {
        viewModelScope.launch { repository.updatePreferences { it.copy(themePalette = palette) } }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.updatePreferences { it.copy(dynamicColor = enabled) } }
    }

    fun setCornerRadius(radius: CardCornerRadius) {
        viewModelScope.launch { repository.updatePreferences { it.copy(cornerRadius = radius) } }
    }

    fun setIconStyle(style: WeatherIconStyle) {
        viewModelScope.launch { repository.updatePreferences { it.copy(iconStyle = style) } }
    }

    fun setParallaxIntensity(intensity: ParallaxIntensity) {
        viewModelScope.launch { repository.updatePreferences { it.copy(parallaxIntensity = intensity) } }
    }

    fun setAnimationIntensity(intensity: AnimationIntensity) {
        viewModelScope.launch { repository.updatePreferences { it.copy(animationIntensity = intensity) } }
    }

    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch { repository.updatePreferences { it.copy(animationsEnabled = enabled) } }
    }

    fun toggleReducedMotion(enabled: Boolean) {
        viewModelScope.launch { repository.updatePreferences { it.copy(reducedMotion = enabled) } }
    }

    fun updateSectionConfig(transform: (HomeScreenSectionConfig) -> HomeScreenSectionConfig) {
        viewModelScope.launch {
            repository.updatePreferences { prefs ->
                prefs.copy(homeSections = transform(prefs.homeSections))
            }
        }
    }

    fun setProvider(provider: WeatherProviderType) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(providerType = provider) }
        }
    }

    fun setCustomApiKey(key: String) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(customApiKey = key.trim()) }
        }
    }

    fun testProviderConnection(type: WeatherProviderType, apiKey: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionTestResult.value = null
            val result = repository.testProviderConnection(type, apiKey)
            _connectionTestResult.value = result.fold(
                onSuccess = { "✓ $it" },
                onFailure = { "✗ ${it.localizedMessage ?: "Connection failed"}" }
            )
            _isTestingConnection.value = false
        }
    }

    fun setRefreshInterval(context: Context, interval: BackgroundRefreshInterval) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(refreshInterval = interval) }
            WeatherSyncWorker.schedule(context, interval)
        }
    }

    fun toggleAlertNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(weatherAlertNotifications = enabled) }
        }
    }

    fun toggleRainNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(rainNotifications = enabled) }
        }
    }

    fun toggleDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(dailySummaryNotification = enabled) }
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

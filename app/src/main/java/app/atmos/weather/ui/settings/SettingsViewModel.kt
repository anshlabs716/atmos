package app.atmos.weather.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.data.model.TimeFormat
import app.atmos.weather.data.model.UserPreferences
import app.atmos.weather.data.model.WindSpeedUnit
import app.atmos.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setTempUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(tempUnit = unit) }
        }
    }

    fun setSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(speedUnit = unit) }
        }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(pressureUnit = unit) }
        }
    }

    fun setTimeFormat(format: TimeFormat) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(timeFormat = format) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(themeMode = mode) }
        }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(dynamicColor = enabled) }
        }
    }

    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(animationsEnabled = enabled) }
        }
    }

    fun toggleGyroscope(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(gyroscopeParallaxEnabled = enabled) }
        }
    }

    fun toggleReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(reducedMotion = enabled) }
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

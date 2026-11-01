package app.atmos.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.DefaultLocations
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.UserPreferences
import app.atmos.weather.data.model.WeatherOverview
import app.atmos.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val overview: WeatherOverview) : WeatherUiState
    data class Error(val message: String, val cachedFallback: WeatherOverview? = null) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val savedLocations: StateFlow<List<LocationItem>> = repository.savedLocationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultLocations)

    private val _selectedLocation = MutableStateFlow(DefaultLocations.first())
    val selectedLocation: StateFlow<LocationItem> = _selectedLocation.asStateFlow()

    init {
        viewModelScope.launch {
            val initial = repository.getSelectedLocation()
            _selectedLocation.value = initial
            loadWeather(initial, forceRefresh = false)
        }
    }

    fun selectLocation(location: LocationItem) {
        if (_selectedLocation.value.id == location.id) return
        _selectedLocation.value = location
        viewModelScope.launch {
            repository.setSelectedLocation(location.id)
            loadWeather(location, forceRefresh = false)
        }
    }

    fun cycleNextLocation() {
        val locations = savedLocations.value
        if (locations.size <= 1) return
        val currentIndex = locations.indexOfFirst { it.id == _selectedLocation.value.id }
        val nextIndex = (currentIndex + 1) % locations.size
        selectLocation(locations[nextIndex])
    }

    fun cyclePrevLocation() {
        val locations = savedLocations.value
        if (locations.size <= 1) return
        val currentIndex = locations.indexOfFirst { it.id == _selectedLocation.value.id }
        val prevIndex = if (currentIndex <= 0) locations.size - 1 else currentIndex - 1
        selectLocation(locations[prevIndex])
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadWeather(_selectedLocation.value, forceRefresh = true)
            _isRefreshing.value = false
        }
    }

    private suspend fun loadWeather(location: LocationItem, forceRefresh: Boolean) {
        if (!forceRefresh && _uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        val result = repository.fetchWeather(location, forceRefresh = forceRefresh)
        result.fold(
            onSuccess = { overview ->
                _uiState.value = WeatherUiState.Success(overview)
            },
            onFailure = { error ->
                val cached = (_uiState.value as? WeatherUiState.Success)?.overview
                _uiState.value = WeatherUiState.Error(
                    message = error.localizedMessage ?: "Failed to load weather data",
                    cachedFallback = cached
                )
            }
        )
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeatherViewModel(repository) as T
        }
    }
}

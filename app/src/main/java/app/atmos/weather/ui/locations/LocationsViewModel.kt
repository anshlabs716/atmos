package app.atmos.weather.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.DefaultLocations
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    val savedLocations: StateFlow<List<LocationItem>> = repository.savedLocationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultLocations)

    val selectedLocationId: StateFlow<String> = repository.selectedLocationIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultLocations.first().id)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationItem>>(emptyList())
    val searchResults: StateFlow<List<LocationItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(350) // debounce typing
            val res = repository.searchCities(query)
            _searchResults.value = res.getOrDefault(emptyList())
            _isSearching.value = false
        }
    }

    fun addLocation(location: LocationItem) {
        viewModelScope.launch {
            repository.addLocation(location)
            _searchQuery.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun removeLocation(locationId: String) {
        viewModelScope.launch {
            repository.removeLocation(locationId)
        }
    }

    fun selectLocation(locationId: String) {
        viewModelScope.launch {
            repository.setSelectedLocation(locationId)
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationsViewModel(repository) as T
        }
    }
}

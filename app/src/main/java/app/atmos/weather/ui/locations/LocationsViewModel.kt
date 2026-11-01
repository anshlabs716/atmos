package app.atmos.weather.ui.locations

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.DefaultLocations
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

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

    fun moveLocationUp(index: Int) {
        if (index > 0) {
            viewModelScope.launch {
                repository.reorderLocation(index, index - 1)
            }
        }
    }

    fun moveLocationDown(index: Int) {
        val size = savedLocations.value.size
        if (index < size - 1) {
            viewModelScope.launch {
                repository.reorderLocation(index, index + 1)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentGpsLocation(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager == null) {
                    onError("Location service unavailable on this device")
                    _isLocating.value = false
                    return@launch
                }

                val lastKnown: Location? = withContext(Dispatchers.IO) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                }

                if (lastKnown != null) {
                    val resolvedLocation = app.atmos.weather.data.location.LocationHelper.reverseGeocode(
                        context = context,
                        latitude = lastKnown.latitude,
                        longitude = lastKnown.longitude
                    )
                    repository.addLocation(resolvedLocation)
                    repository.setSelectedLocation(resolvedLocation.id)
                    onSuccess()
                } else {
                    onError("Could not acquire GPS fix. Please ensure Location is enabled in device settings.")
                }
            } catch (e: Exception) {
                onError("Failed to acquire location: ${e.localizedMessage}")
            } finally {
                _isLocating.value = false
            }
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationsViewModel(repository) as T
        }
    }
}

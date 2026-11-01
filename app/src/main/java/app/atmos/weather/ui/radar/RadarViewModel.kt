package app.atmos.weather.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadarTimelineFrame(
    val id: Int,
    val timeLabel: String,
    val isPast: Boolean,
    val isCurrent: Boolean
)

val DefaultTimelineFrames = listOf(
    RadarTimelineFrame(0, "-90m", isPast = true, isCurrent = false),
    RadarTimelineFrame(1, "-60m", isPast = true, isCurrent = false),
    RadarTimelineFrame(2, "-30m", isPast = true, isCurrent = false),
    RadarTimelineFrame(3, "Now", isPast = false, isCurrent = true),
    RadarTimelineFrame(4, "+30m", isPast = false, isCurrent = false),
    RadarTimelineFrame(5, "+60m", isPast = false, isCurrent = false)
)

class RadarViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<LocationItem?>(null)
    val selectedLocation: StateFlow<LocationItem?> = _selectedLocation.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(3) // "Now" frame default
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private var animationJob: Job? = null

    init {
        viewModelScope.launch {
            _selectedLocation.value = repository.getSelectedLocation()
        }
    }

    fun selectFrame(index: Int) {
        _currentFrameIndex.value = index.coerceIn(0, DefaultTimelineFrames.size - 1)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _isPlaying.value = true
        animationJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(750)
                val next = (_currentFrameIndex.value + 1) % DefaultTimelineFrames.size
                _currentFrameIndex.value = next
            }
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        animationJob?.cancel()
        animationJob = null
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value * 1.25f).coerceAtMost(3.0f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value / 1.25f).coerceAtLeast(0.6f)
    }

    fun resetView() {
        _zoomLevel.value = 1.0f
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RadarViewModel(repository) as T
        }
    }
}

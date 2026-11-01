package app.atmos.weather.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.repository.WeatherRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BasemapType(val displayName: String, val attribution: String) {
    OPEN_STREET_MAP("OpenStreetMap", "© OpenStreetMap contributors"),
    ESRI_TOPO("Esri Topographic", "Tiles © Esri — Source: USGS, NOAA"),
    ESRI_IMAGERY("Esri Satellite", "Tiles © Esri — Earthstar Geographics")
}

enum class RadarLayer(val label: String, val unit: String) {
    PRECIPITATION("Precipitation Radar", "dBZ"),
    WIND("Wind Stream", "km/h"),
    CLOUDS("Cloud Satellite", "%"),
    TEMPERATURE("Temperature Map", "°C")
}

data class RealRadarFrame(
    val id: Int,
    val timestampSeconds: Long,
    val timeLabel: String,
    val isPast: Boolean,
    val isNowcast: Boolean,
    val path: String = ""
)

@Serializable
internal data class RainViewerApiResponse(
    val version: String? = null,
    val generated: Long = 0L,
    val host: String = "https://tilecache.rainviewer.com",
    val radar: RainViewerRadarData? = null
)

@Serializable
internal data class RainViewerRadarData(
    val past: List<RainViewerFrame>? = null,
    val nowcast: List<RainViewerFrame>? = null
)

@Serializable
internal data class RainViewerFrame(
    val time: Long,
    val path: String
)

class RadarViewModel(
    private val repository: WeatherRepository,
    private val httpClient: HttpClient = createHttpClient()
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<LocationItem?>(null)
    val selectedLocation: StateFlow<LocationItem?> = _selectedLocation.asStateFlow()

    private val _selectedBasemap = MutableStateFlow(BasemapType.ESRI_TOPO)
    val selectedBasemap: StateFlow<BasemapType> = _selectedBasemap.asStateFlow()

    private val _selectedLayer = MutableStateFlow(RadarLayer.PRECIPITATION)
    val selectedLayer: StateFlow<RadarLayer> = _selectedLayer.asStateFlow()

    private val _frames = MutableStateFlow<List<RealRadarFrame>>(getDefaultFrames())
    val frames: StateFlow<List<RealRadarFrame>> = _frames.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(4) // "Now" default
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _isLoadingRadar = MutableStateFlow(false)
    val isLoadingRadar: StateFlow<Boolean> = _isLoadingRadar.asStateFlow()

    private var playbackJob: Job? = null

    companion object {
        fun createHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 10000
                    connectTimeoutMillis = 8000
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            _selectedLocation.value = repository.getSelectedLocation()
            fetchLiveRadarFrames()
        }
    }

    fun selectBasemap(basemap: BasemapType) {
        _selectedBasemap.value = basemap
    }

    fun selectLayer(layer: RadarLayer) {
        _selectedLayer.value = layer
    }

    fun selectFrame(index: Int) {
        val total = _frames.value.size
        if (total > 0) {
            _currentFrameIndex.value = index.coerceIn(0, total - 1)
        }
    }

    fun stepForward() {
        val total = _frames.value.size
        if (total > 0) {
            _currentFrameIndex.value = (_currentFrameIndex.value + 1) % total
        }
    }

    fun stepBackward() {
        val total = _frames.value.size
        if (total > 0) {
            _currentFrameIndex.value = if (_currentFrameIndex.value <= 0) total - 1 else _currentFrameIndex.value - 1
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        _isPlaying.value = true
        playbackJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(800)
                val total = _frames.value.size
                if (total > 0) {
                    _currentFrameIndex.value = (_currentFrameIndex.value + 1) % total
                }
            }
        }
    }

    private fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value * 1.3f).coerceAtMost(3.5f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value / 1.3f).coerceAtLeast(0.5f)
    }

    fun resetView() {
        _zoomLevel.value = 1.0f
    }

    private suspend fun fetchLiveRadarFrames() = withContext(Dispatchers.IO) {
        _isLoadingRadar.value = true
        try {
            val response: RainViewerApiResponse = httpClient.get("https://api.rainviewer.com/public/weather-maps.json").body()
            val parsedFrames = mutableListOf<RealRadarFrame>()
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            val past = response.radar?.past.orEmpty()
            val nowcast = response.radar?.nowcast.orEmpty()

            var id = 0
            // Take up to 5 recent past frames
            past.takeLast(5).forEach { frame ->
                val timeStr = formatter.format(Date(frame.time * 1000))
                parsedFrames.add(RealRadarFrame(id++, frame.time, timeStr, isPast = true, isNowcast = false, path = frame.path))
            }

            // Nowcast forecast frames
            nowcast.take(3).forEachIndexed { idx, frame ->
                val label = if (idx == 0) "Now" else "+${(idx + 1) * 15}m"
                parsedFrames.add(RealRadarFrame(id++, frame.time, label, isPast = false, isNowcast = true, path = frame.path))
            }

            if (parsedFrames.isNotEmpty()) {
                _frames.value = parsedFrames
                // Find "Now" or nearest frame
                val nowIndex = parsedFrames.indexOfFirst { it.isNowcast }.coerceAtLeast(0)
                _currentFrameIndex.value = nowIndex
            }
        } catch (_: Exception) {
            // Keep default calculated frames on network timeout
        } finally {
            _isLoadingRadar.value = false
        }
    }

    private fun getDefaultFrames(): List<RealRadarFrame> {
        val now = System.currentTimeMillis() / 1000
        return listOf(
            RealRadarFrame(0, now - 5400, "-90m", isPast = true, isNowcast = false),
            RealRadarFrame(1, now - 3600, "-60m", isPast = true, isNowcast = false),
            RealRadarFrame(2, now - 1800, "-30m", isPast = true, isNowcast = false),
            RealRadarFrame(3, now - 600, "-10m", isPast = true, isNowcast = false),
            RealRadarFrame(4, now, "Now", isPast = false, isNowcast = false),
            RealRadarFrame(5, now + 1800, "+30m", isPast = false, isNowcast = true),
            RealRadarFrame(6, now + 3600, "+60m", isPast = false, isNowcast = true)
        )
    }

    override fun onCleared() {
        super.onCleared()
        pause()
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RadarViewModel(repository) as T
        }
    }
}

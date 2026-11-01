package app.atmos.weather.data.local

import android.content.Context
import app.atmos.weather.data.model.WeatherOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class WeatherCache(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cacheDir: File
        get() = File(context.filesDir, "weather_cache").apply { if (!exists()) mkdirs() }

    suspend fun saveWeather(overview: WeatherOverview) = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "${overview.location.id}.json")
            val data = json.encodeToString(overview.copy(isCached = true))
            file.writeText(data)
        } catch (_: Exception) {}
    }

    suspend fun getWeather(locationId: String): WeatherOverview? = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "$locationId.json")
            if (file.exists()) {
                val content = file.readText()
                json.decodeFromString<WeatherOverview>(content)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

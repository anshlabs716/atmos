package app.atmos.weather

import android.app.Application
import app.atmos.weather.data.local.PreferencesManager
import app.atmos.weather.data.local.WeatherCache
import app.atmos.weather.data.provider.OpenMeteoProvider
import app.atmos.weather.data.repository.WeatherRepository

class AtmosApplication : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set
    lateinit var weatherCache: WeatherCache
        private set
    lateinit var weatherRepository: WeatherRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        weatherCache = WeatherCache(this)
        val provider = OpenMeteoProvider()
        weatherRepository = WeatherRepository(
            preferencesManager = preferencesManager,
            weatherCache = weatherCache,
            provider = provider
        )
    }
}

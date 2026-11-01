package app.atmos.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.ui.locations.LocationsViewModel
import app.atmos.weather.ui.navigation.AppNavigation
import app.atmos.weather.ui.radar.RadarViewModel
import app.atmos.weather.ui.settings.SettingsViewModel
import app.atmos.weather.ui.theme.AtmosTheme
import app.atmos.weather.ui.weather.WeatherViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AtmosApplication
        val repository = app.weatherRepository

        val weatherViewModel by viewModels<WeatherViewModel> {
            WeatherViewModel.Factory(repository)
        }
        val radarViewModel by viewModels<RadarViewModel> {
            RadarViewModel.Factory(repository)
        }
        val locationsViewModel by viewModels<LocationsViewModel> {
            LocationsViewModel.Factory(repository)
        }
        val settingsViewModel by viewModels<SettingsViewModel> {
            SettingsViewModel.Factory(repository)
        }

        setContent {
            val preferences by settingsViewModel.userPreferences.collectAsStateWithLifecycle()

            val isDark = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AtmosTheme(
                darkTheme = isDark,
                dynamicColor = preferences.dynamicColor
            ) {
                AppNavigation(
                    weatherViewModel = weatherViewModel,
                    radarViewModel = radarViewModel,
                    locationsViewModel = locationsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

package app.atmos.weather.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.atmos.weather.ui.locations.LocationsScreen
import app.atmos.weather.ui.locations.LocationsViewModel
import app.atmos.weather.ui.radar.RadarScreen
import app.atmos.weather.ui.radar.RadarViewModel
import app.atmos.weather.ui.settings.SettingsScreen
import app.atmos.weather.ui.settings.SettingsViewModel
import app.atmos.weather.ui.weather.WeatherScreen
import app.atmos.weather.ui.weather.WeatherViewModel

@Composable
fun AppNavigation(
    weatherViewModel: WeatherViewModel,
    radarViewModel: RadarViewModel,
    locationsViewModel: LocationsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by rememberSaveable { mutableStateOf(NavigationDestination.WEATHER) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentDestination,
                label = "navigation_crossfade"
            ) { destination ->
                when (destination) {
                    NavigationDestination.WEATHER -> {
                        WeatherScreen(
                            viewModel = weatherViewModel,
                            onNavigateToLocations = { currentDestination = NavigationDestination.LOCATIONS }
                        )
                    }
                    NavigationDestination.RADAR -> {
                        RadarScreen(
                            viewModel = radarViewModel
                        )
                    }
                    NavigationDestination.LOCATIONS -> {
                        LocationsScreen(
                            viewModel = locationsViewModel,
                            onLocationSelected = { currentDestination = NavigationDestination.WEATHER }
                        )
                    }
                    NavigationDestination.SETTINGS -> {
                        SettingsScreen(
                            viewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}

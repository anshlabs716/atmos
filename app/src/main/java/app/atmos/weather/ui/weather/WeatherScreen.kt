package app.atmos.weather.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.atmos.weather.data.model.LocationItem
import app.atmos.weather.data.model.WeatherOverview
import app.atmos.weather.sensors.rememberGyroscopeParallax
import androidx.compose.material.icons.filled.Info
import app.atmos.weather.ui.components.AirQualityCard
import app.atmos.weather.ui.components.DailyForecastSection
import app.atmos.weather.ui.components.DetailMetricCards
import app.atmos.weather.ui.components.HourlyForecastSection
import app.atmos.weather.ui.components.MoonCard
import app.atmos.weather.ui.components.PrecipitationSection
import app.atmos.weather.ui.components.SunriseSunsetArc
import app.atmos.weather.ui.components.WeatherAlertBanner
import app.atmos.weather.ui.components.WeatherHero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onNavigateToLocations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

    val parallax = rememberGyroscopeParallax(
        intensity = preferences.parallaxIntensity,
        reducedMotion = preferences.reducedMotion
    )

    var totalDrag by remember { mutableFloatStateOf(0f) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(savedLocations) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (totalDrag > 120f) {
                            viewModel.cyclePrevLocation()
                        } else if (totalDrag < -120f) {
                            viewModel.cycleNextLocation()
                        }
                        totalDrag = 0f
                    },
                    onDragCancel = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
            .testTag("weather_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            // Top App Bar with City Selector Dropdown & Refresh
            WeatherTopBar(
                selectedLocation = selectedLocation,
                savedLocations = savedLocations,
                onSelectLocation = { viewModel.selectLocation(it) },
                onManageLocations = onNavigateToLocations,
                onRefresh = { viewModel.refresh() },
                isRefreshing = isRefreshing
            )

            // Content Area based on State
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Loading atmospheric data…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    if (state.cachedFallback != null) {
                        WeatherContent(
                            overview = state.cachedFallback.copy(isCached = true),
                            preferences = preferences,
                            parallax = parallax.value,
                            errorMessage = state.message
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = "Unable to load forecast",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    onClick = { viewModel.refresh() },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "Retry",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    WeatherContent(
                        overview = state.overview,
                        preferences = preferences,
                        parallax = parallax.value,
                        errorMessage = null
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherTopBar(
    selectedLocation: LocationItem,
    savedLocations: List<LocationItem>,
    onSelectLocation: (LocationItem) -> Unit,
    onManageLocations: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Location Dropdown Selector Pill
        Box {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { menuExpanded = true }
                    .testTag("location_dropdown_button"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Current location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = selectedLocation.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select City",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                savedLocations.forEach { loc ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = loc.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (loc.id == selectedLocation.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onSelectLocation(loc)
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "+ Manage saved locations",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onManageLocations()
                    }
                )
            }
        }

        // City Carousel Pager Dots & Refresh Action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Dots
            savedLocations.take(5).forEach { loc ->
                Box(
                    modifier = Modifier
                        .size(if (loc.id == selectedLocation.id) 8.dp else 5.dp)
                        .background(
                            if (loc.id == selectedLocation.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                )
            }

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh weather",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeatherContent(
    overview: WeatherOverview,
    preferences: app.atmos.weather.data.model.UserPreferences,
    parallax: app.atmos.weather.sensors.ParallaxOffset,
    errorMessage: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Offline / Cached Mode Banner
        if (overview.isCached || errorMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline cached data",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val cachedTime = timeFormat.format(Date(overview.fetchedAtEpochMillis))
                            Text(
                                text = "Offline Mode • Showing cached forecast",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Last updated at $cachedTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 1. Weather Alerts (Visible only when present)
        if (overview.alerts.isNotEmpty()) {
            item {
                WeatherAlertBanner(alerts = overview.alerts)
            }
        }

        // 2. Weather Hero Centrepiece
        item {
            WeatherHero(
                current = overview.current,
                todayForecast = overview.daily.firstOrNull(),
                tempUnit = preferences.tempUnit,
                parallax = parallax,
                animationsEnabled = preferences.animationsEnabled,
                animationIntensity = preferences.animationIntensity,
                reducedMotion = preferences.reducedMotion
            )
        }

        // 3. Hourly Forecast Cards
        if (preferences.homeSections.showHourly) {
            item {
                HourlyForecastSection(
                    hourly = overview.hourly,
                    tempUnit = preferences.tempUnit
                )
            }
        }

        // 4. Precipitation Cards
        if (preferences.homeSections.showPrecipitation) {
            item {
                PrecipitationSection(
                    todayDaily = overview.daily.firstOrNull(),
                    upcomingHourly = overview.hourly,
                    precipUnit = preferences.precipUnit
                )
            }
        }

        // 5. Seven-Day Forecast Rows
        if (preferences.homeSections.showDaily) {
            item {
                DailyForecastSection(
                    daily = overview.daily,
                    tempUnit = preferences.tempUnit
                )
            }
        }

        // 6. Mixed-size Weather Detail Cards
        if (preferences.homeSections.showDetails) {
            item {
                DetailMetricCards(
                    current = overview.current,
                    tempUnit = preferences.tempUnit,
                    speedUnit = preferences.speedUnit,
                    pressureUnit = preferences.pressureUnit,
                    visibilityUnit = preferences.visibilityUnit
                )
            }
        }

        // 7. Sunrise and Sunset Arc
        if (preferences.homeSections.showSunriseSunset) {
            item {
                SunriseSunsetArc(
                    data = overview.sunriseSunset,
                    parallax = parallax
                )
            }
        }

        // 8. Air Quality Card
        if (preferences.homeSections.showAirQuality && overview.airQuality != null) {
            item {
                AirQualityCard(airQuality = overview.airQuality)
            }
        }

        // 9. Provider Attribution Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Weather data provided by ${preferences.providerType.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Atmos • Weather, beautifully understood",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

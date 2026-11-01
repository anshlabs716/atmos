package app.atmos.weather.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.atmos.weather.data.model.AnimationIntensity
import app.atmos.weather.data.model.BackgroundRefreshInterval
import app.atmos.weather.data.model.CardCornerRadius
import app.atmos.weather.data.model.ParallaxIntensity
import app.atmos.weather.data.model.PrecipitationUnit
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.data.model.ThemePalette
import app.atmos.weather.data.model.TimeFormat
import app.atmos.weather.data.model.VisibilityUnit
import app.atmos.weather.data.model.WeatherIconStyle
import app.atmos.weather.data.model.WeatherProviderType
import app.atmos.weather.data.model.WindSpeedUnit

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val connectionTestResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleAlertNotifications(true)
        }
    }

    // Runtime Permission Launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // Section 1: Units of Measurement
        item {
            SettingsCategoryCard(title = "Units of Measurement") {
                // Temperature
                UnitSelectorRow(
                    label = "Temperature",
                    icon = Icons.Outlined.Thermostat,
                    options = listOf(
                        TemperatureUnit.CELSIUS to "Celsius (°C)",
                        TemperatureUnit.FAHRENHEIT to "Fahrenheit (°F)"
                    ),
                    selected = preferences.tempUnit,
                    onSelect = { viewModel.setTempUnit(it) }
                )

                // Wind Speed
                UnitSelectorRow(
                    label = "Wind Speed",
                    icon = Icons.Outlined.Speed,
                    options = listOf(
                        WindSpeedUnit.KMH to "km/h",
                        WindSpeedUnit.MPH to "mph",
                        WindSpeedUnit.MS to "m/s",
                        WindSpeedUnit.KNOTS to "kn"
                    ),
                    selected = preferences.speedUnit,
                    onSelect = { viewModel.setSpeedUnit(it) }
                )

                // Precipitation
                UnitSelectorRow(
                    label = "Precipitation",
                    icon = Icons.Outlined.WaterDrop,
                    options = listOf(
                        PrecipitationUnit.MILLIMETERS to "Millimeters (mm)",
                        PrecipitationUnit.INCHES to "Inches (in)"
                    ),
                    selected = preferences.precipUnit,
                    onSelect = { viewModel.setPrecipUnit(it) }
                )

                // Pressure
                UnitSelectorRow(
                    label = "Atmospheric Pressure",
                    icon = Icons.Outlined.Compress,
                    options = listOf(
                        PressureUnit.HPA to "hPa",
                        PressureUnit.INHG to "inHg",
                        PressureUnit.MMHG to "mmHg",
                        PressureUnit.MBAR to "mbar"
                    ),
                    selected = preferences.pressureUnit,
                    onSelect = { viewModel.setPressureUnit(it) }
                )

                // Visibility
                UnitSelectorRow(
                    label = "Visibility",
                    icon = Icons.Outlined.Visibility,
                    options = listOf(
                        VisibilityUnit.KILOMETERS to "Kilometers (km)",
                        VisibilityUnit.MILES to "Miles (mi)"
                    ),
                    selected = preferences.visibilityUnit,
                    onSelect = { viewModel.setVisibilityUnit(it) }
                )

                // Time Display
                UnitSelectorRow(
                    label = "Time Display",
                    icon = Icons.Outlined.Schedule,
                    options = listOf(
                        TimeFormat.H24 to "24-Hour (14:30)",
                        TimeFormat.H12 to "12-Hour (2:30 PM)"
                    ),
                    selected = preferences.timeFormat,
                    onSelect = { viewModel.setTimeFormat(it) }
                )
            }
        }

        // Section 2: Appearance & Theme
        item {
            SettingsCategoryCard(title = "Appearance & Theme") {
                UnitSelectorRow(
                    label = "Theme Mode",
                    icon = Icons.Outlined.DarkMode,
                    options = listOf(
                        ThemeMode.SYSTEM to "System",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK to "Dark"
                    ),
                    selected = preferences.themeMode,
                    onSelect = { viewModel.setThemeMode(it) }
                )

                // Curated Accent Palettes
                UnitSelectorRow(
                    label = "Curated Palette",
                    icon = Icons.Outlined.ColorLens,
                    options = listOf(
                        ThemePalette.ATMOS_BLUE to "Atmos Blue",
                        ThemePalette.EMERALD_AURORA to "Emerald",
                        ThemePalette.SUNSET_CORAL to "Coral",
                        ThemePalette.LAVENDER_DUSK to "Lavender",
                        ThemePalette.OBSIDIAN_SLATE to "Slate"
                    ),
                    selected = preferences.themePalette,
                    onSelect = { viewModel.setThemePalette(it) }
                )

                // Card Corner Radius
                UnitSelectorRow(
                    label = "Card Shape Style",
                    icon = Icons.Outlined.RoundedCorner,
                    options = listOf(
                        CardCornerRadius.EXPRESSIVE to "Expressive (28dp)",
                        CardCornerRadius.SOFT to "Soft (20dp)",
                        CardCornerRadius.CRISP to "Crisp (12dp)"
                    ),
                    selected = preferences.cornerRadius,
                    onSelect = { viewModel.setCornerRadius(it) }
                )

                // Weather Icon Style
                UnitSelectorRow(
                    label = "Weather Icon Style",
                    icon = Icons.Outlined.Palette,
                    options = listOf(
                        WeatherIconStyle.ROUNDED to "Rounded",
                        WeatherIconStyle.OUTLINED to "Outlined",
                        WeatherIconStyle.FILLED to "Filled"
                    ),
                    selected = preferences.iconStyle,
                    onSelect = { viewModel.setIconStyle(it) }
                )

                SettingSwitchRow(
                    title = "Dynamic Material You Colors",
                    subtitle = "Extract tones from your device wallpaper (Android 12+)",
                    icon = Icons.Outlined.Palette,
                    checked = preferences.dynamicColor,
                    onCheckedChange = { viewModel.toggleDynamicColor(it) }
                )
            }
        }

        // Section 3: Gyroscope & Motion Customization
        item {
            SettingsCategoryCard(title = "Gyroscope & Motion Customization") {
                // Parallax Intensity Selector
                UnitSelectorRow(
                    label = "Gyroscope Parallax Intensity",
                    icon = Icons.Outlined.ScreenRotation,
                    options = listOf(
                        ParallaxIntensity.OFF to "Off",
                        ParallaxIntensity.SUBTLE to "Subtle",
                        ParallaxIntensity.BALANCED to "Balanced (2.2x)",
                        ParallaxIntensity.STRONG to "Strong (4.5x)"
                    ),
                    selected = preferences.parallaxIntensity,
                    onSelect = { viewModel.setParallaxIntensity(it) }
                )

                // Animation Intensity Selector
                UnitSelectorRow(
                    label = "Animation Pace",
                    icon = Icons.Outlined.Animation,
                    options = listOf(
                        AnimationIntensity.GENTLE to "Gentle",
                        AnimationIntensity.NORMAL to "Normal",
                        AnimationIntensity.EXPRESSIVE to "Expressive"
                    ),
                    selected = preferences.animationIntensity,
                    onSelect = { viewModel.setAnimationIntensity(it) }
                )

                SettingSwitchRow(
                    title = "Weather Animations",
                    subtitle = "Animated drifting clouds, rain particles, and sunlight pulse",
                    icon = Icons.Outlined.Animation,
                    checked = preferences.animationsEnabled,
                    onCheckedChange = { viewModel.toggleAnimations(it) }
                )

                SettingSwitchRow(
                    title = "Reduced Motion",
                    subtitle = "Disables camera tilting and high-speed motion for accessibility",
                    icon = Icons.Outlined.Palette,
                    checked = preferences.reducedMotion,
                    onCheckedChange = { viewModel.toggleReducedMotion(it) }
                )
            }
        }

        // Section 4: Home Screen Customization
        item {
            SettingsCategoryCard(title = "Home Screen Customization") {
                Text(
                    text = "Configure which dashboard sections are visible. Critical active severe warnings are always displayed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val sections = preferences.homeSections

                SettingSwitchRow(
                    title = "Hourly Forecast Row",
                    subtitle = "Show 24-hour horizontal forecast cards",
                    icon = Icons.Outlined.Schedule,
                    checked = sections.showHourly,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showHourly = checked) }
                    }
                )

                SettingSwitchRow(
                    title = "Precipitation Outlook",
                    subtitle = "Show expected rainfall & probability bars",
                    icon = Icons.Outlined.WaterDrop,
                    checked = sections.showPrecipitation,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showPrecipitation = checked) }
                    }
                )

                SettingSwitchRow(
                    title = "7-Day Daily Forecast",
                    subtitle = "Show week-ahead weather and temperature ranges",
                    icon = Icons.Outlined.Dashboard,
                    checked = sections.showDaily,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showDaily = checked) }
                    }
                )

                SettingSwitchRow(
                    title = "Weather Details Grid",
                    subtitle = "Show Wind compass, UV Index, Humidity, and Pressure",
                    icon = Icons.Outlined.Speed,
                    checked = sections.showDetails,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showDetails = checked) }
                    }
                )

                SettingSwitchRow(
                    title = "Sun Schedule Arc",
                    subtitle = "Show visual sunrise, sunset, and daylight arc",
                    icon = Icons.Outlined.Thermostat,
                    checked = sections.showSunriseSunset,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showSunriseSunset = checked) }
                    }
                )

                SettingSwitchRow(
                    title = "Air Quality Summary",
                    subtitle = "Show AQI rating and PM2.5 details",
                    icon = Icons.Outlined.Cloud,
                    checked = sections.showAirQuality,
                    onCheckedChange = { checked ->
                        viewModel.updateSectionConfig { it.copy(showAirQuality = checked) }
                    }
                )
            }
        }

        // Section 5: Weather Providers & API Keys
        item {
            SettingsCategoryCard(title = "Weather Providers & Connectivity") {
                // Provider Selector
                UnitSelectorRow(
                    label = "Active Weather Provider",
                    icon = Icons.Outlined.Cloud,
                    options = listOf(
                        WeatherProviderType.OPEN_METEO to "Open-Meteo",
                        WeatherProviderType.BOM_AUSTRALIA to "BOM Australia",
                        WeatherProviderType.OPEN_WEATHER_MAP to "OpenWeatherMap",
                        WeatherProviderType.WEATHER_API to "WeatherAPI"
                    ),
                    selected = preferences.providerType,
                    onSelect = { viewModel.setProvider(it) }
                )

                // Custom API Key Input if required
                if (!preferences.providerType.isFreeNoKey) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = preferences.customApiKey,
                            onValueChange = { viewModel.setCustomApiKey(it) },
                            label = { Text("Custom API Key (${preferences.providerType.displayName})") },
                            placeholder = { Text("Enter your API key…") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Key, contentDescription = "API Key")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Test Connection Button with Real Network Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.testProviderConnection(
                                preferences.providerType,
                                preferences.customApiKey
                            )
                        },
                        enabled = !isTestingConnection,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Test Connectivity")
                    }

                    Text(
                        text = if (preferences.providerType.isFreeNoKey) "Free • No Key Needed" else "Key Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Test Connection Result Banner
                AnimatedVisibility(visible = connectionTestResult != null) {
                    connectionTestResult?.let { result ->
                        Surface(
                            color = if (result.startsWith("✓")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (result.startsWith("✓")) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Attribution: ${preferences.providerType.defaultAttribution}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 6: Permissions & Background Sync
        item {
            SettingsCategoryCard(title = "Permissions & Background Sync") {
                // Check actual system permission state
                val hasFineLoc = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasCoarseLoc = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                // Location Permission Status Row
                PermissionStatusItem(
                    title = "Location Permission",
                    description = "Used to detect local weather. Manual city selection works without GPS.",
                    icon = Icons.Outlined.LocationOn,
                    isGranted = hasFineLoc || hasCoarseLoc,
                    onRequest = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                // Notification Permission Status Row
                PermissionStatusItem(
                    title = "Notification Permission",
                    description = "Used for severe weather warnings (storms, excessive heat, hard freeze).",
                    icon = Icons.Outlined.Notifications,
                    isGranted = hasNotification,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )

                // Background Refresh Interval
                UnitSelectorRow(
                    label = "Background Refresh Interval",
                    icon = Icons.Outlined.Sync,
                    options = listOf(
                        BackgroundRefreshInterval.OFF to "Off",
                        BackgroundRefreshInterval.HOURS_1 to "1 Hour",
                        BackgroundRefreshInterval.HOURS_2 to "2 Hours",
                        BackgroundRefreshInterval.HOURS_4 to "4 Hours",
                        BackgroundRefreshInterval.HOURS_8 to "8 Hours"
                    ),
                    selected = preferences.refreshInterval,
                    onSelect = { viewModel.setRefreshInterval(context, it) }
                )

                SettingSwitchRow(
                    title = "Severe Weather Alert Notifications",
                    subtitle = "Alert you when dangerous storms or high winds are detected",
                    icon = Icons.Outlined.Notifications,
                    checked = preferences.weatherAlertNotifications,
                    onCheckedChange = { viewModel.toggleAlertNotifications(it) }
                )

                // Open Android System Settings Action
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Open Android App Settings")
                }
            }
        }

        // Section 7: Privacy & Open Source
        item {
            SettingsCategoryCard(title = "Privacy & Open Source") {
                SettingActionRow(
                    title = "Privacy Policy",
                    subtitle = "No ads, no tracking, zero accounts, 100% on-device storage",
                    icon = Icons.Outlined.Security,
                    onClick = { showPrivacyDialog = true }
                )

                SettingActionRow(
                    title = "About Atmos",
                    subtitle = "Version 1.0.0 • GPL-3.0-or-later • Open-Meteo & RainViewer",
                    icon = Icons.Outlined.Info,
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Atmos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Weather, beautifully understood.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Atmos is an open-source, privacy-first Android weather app built with Jetpack Compose, Material 3, and expressive atmospheric artwork.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Weather Data: Open-Meteo (CC BY 4.0)\n• Radar Imagery: RainViewer & Esri\n• License: GPL-3.0-or-later\n• Platform: Native Android (API 34+)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy-First Promise") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Atmos is engineered strictly for user privacy:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✓ Zero Advertising & Zero Trackers\n✓ No Account or Sign-in Required\n✓ Saved cities stored exclusively on-device\n✓ Location access is strictly optional\n✓ Weather data fetched directly from Open-Meteo without intermediaries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun <T> UnitSelectorRow(
    label: String,
    icon: ImageVector,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (option, optLabel) ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    label = { Text(optLabel, fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionStatusItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        if (isGranted) {
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        } else {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Allow", fontSize = 12.sp)
            }
        }
    }
}

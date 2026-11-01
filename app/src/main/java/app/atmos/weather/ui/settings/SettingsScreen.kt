package app.atmos.weather.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.ThemeMode
import app.atmos.weather.data.model.TimeFormat
import app.atmos.weather.data.model.WindSpeedUnit

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

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
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Section 1: Units
        item {
            SettingsCategoryCard(title = "Units of Measurement") {
                // Temperature Unit
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

                Spacer(modifier = Modifier.height(8.dp))

                // Wind Speed Unit
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

                Spacer(modifier = Modifier.height(8.dp))

                // Pressure Unit
                UnitSelectorRow(
                    label = "Atmospheric Pressure",
                    icon = Icons.Outlined.Compress,
                    options = listOf(
                        PressureUnit.HPA to "hPa",
                        PressureUnit.INHG to "inHg",
                        PressureUnit.MMHG to "mmHg"
                    ),
                    selected = preferences.pressureUnit,
                    onSelect = { viewModel.setPressureUnit(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Format
                UnitSelectorRow(
                    label = "Time Display",
                    icon = Icons.Outlined.Schedule,
                    options = listOf(
                        TimeFormat.H24 to "24-Hour",
                        TimeFormat.H12 to "12-Hour"
                    ),
                    selected = preferences.timeFormat,
                    onSelect = { viewModel.setTimeFormat(it) }
                )
            }
        }

        // Section 2: Appearance & Motion
        item {
            SettingsCategoryCard(title = "Theme & Visual Effects") {
                // Theme Mode
                UnitSelectorRow(
                    label = "Appearance",
                    icon = Icons.Outlined.DarkMode,
                    options = listOf(
                        ThemeMode.SYSTEM to "System",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK to "Dark"
                    ),
                    selected = preferences.themeMode,
                    onSelect = { viewModel.setThemeMode(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gyroscope Parallax Toggle
                SettingSwitchRow(
                    title = "Gyroscope Parallax",
                    subtitle = "Tilt artwork smoothly using device rotation vector sensor",
                    icon = Icons.Outlined.ScreenRotation,
                    checked = preferences.gyroscopeParallaxEnabled,
                    onCheckedChange = { viewModel.toggleGyroscope(it) }
                )

                // Weather Reactive Animations Toggle
                SettingSwitchRow(
                    title = "Weather Animations",
                    subtitle = "Subtle drifting clouds, rain drops, and sunlight pulse",
                    icon = Icons.Outlined.Animation,
                    checked = preferences.animationsEnabled,
                    onCheckedChange = { viewModel.toggleAnimations(it) }
                )

                // Reduced Motion Toggle
                SettingSwitchRow(
                    title = "Reduced Motion",
                    subtitle = "Disables all dynamic camera tilting and high-motion effects",
                    icon = Icons.Outlined.Palette,
                    checked = preferences.reducedMotion,
                    onCheckedChange = { viewModel.toggleReducedMotion(it) }
                )
            }
        }

        // Section 3: Privacy & Open Source
        item {
            SettingsCategoryCard(title = "Privacy & Open Source") {
                // Privacy Info row
                SettingActionRow(
                    title = "Privacy Policy",
                    subtitle = "No advertising, no tracking, zero accounts, 100% private",
                    icon = Icons.Outlined.Security,
                    onClick = { showPrivacyDialog = true }
                )

                // About Atmos row
                SettingActionRow(
                    title = "About Atmos",
                    subtitle = "Version 1.0.0 • GPL-3.0-or-later • Open-Meteo",
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
                        text = "• Weather Data: Open-Meteo (CC BY 4.0)\n• License: GPL-3.0-or-later\n• Platform: Native Android (API 34+)",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (option, optLabel) ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    label = { Text(optLabel, fontSize = 12.sp) },
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

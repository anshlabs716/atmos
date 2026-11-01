package app.atmos.weather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.atmos.weather.data.model.CurrentWeather
import app.atmos.weather.data.model.PressureUnit
import app.atmos.weather.data.model.TemperatureUnit
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.atmos.weather.data.model.PressureTrend
import app.atmos.weather.data.model.VisibilityUnit
import app.atmos.weather.data.model.WindSpeedUnit

@Composable
fun DetailMetricCards(
    current: CurrentWeather,
    tempUnit: TemperatureUnit,
    speedUnit: WindSpeedUnit,
    pressureUnit: PressureUnit,
    visibilityUnit: VisibilityUnit,
    modifier: Modifier = Modifier
) {
    var showPressureDialog by remember { mutableStateOf(false) }
    var showUvDialog by remember { mutableStateOf(false) }
    var showWindDialog by remember { mutableStateOf(false) }
    var showHumidityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Weather Conditions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Row 1: Large Feature Cards (Wind & Compass + UV Index Gauge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wind Card with Compass
            WindFeatureCard(
                speedKmh = current.windSpeed,
                direction = current.windDirection,
                speedUnit = speedUnit,
                onClick = { showWindDialog = true },
                modifier = Modifier.weight(1f)
            )

            // UV Index Card
            UvIndexFeatureCard(
                uvIndex = current.uvIndex,
                onClick = { showUvDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Two Compact Metric Cards (Feels Like & Humidity)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactMetricCard(
                title = "Feels Like",
                value = tempUnit.format(current.apparentTemperature),
                subtitle = if (current.apparentTemperature > current.temperature) "Humid & warmer" else "Wind & cooler",
                icon = Icons.Outlined.DeviceThermostat,
                onClick = {},
                modifier = Modifier.weight(1f)
            )

            CompactMetricCard(
                title = "Humidity",
                value = "${current.humidity}%",
                subtitle = "Dew point is ${tempUnit.format(current.dewPoint)}",
                icon = Icons.Outlined.Opacity,
                onClick = { showHumidityDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Two Compact Metric Cards (Pressure & Visibility)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val pressureComparison = if (current.pressure >= 1013.25) "Above standard" else "Below standard"
            CompactMetricCard(
                title = "Air Pressure",
                value = pressureUnit.format(current.pressure),
                subtitle = "${current.pressureTrend.label} • $pressureComparison",
                icon = Icons.Outlined.Compress,
                onClick = { showPressureDialog = true },
                modifier = Modifier.weight(1f)
            )

            CompactMetricCard(
                title = "Visibility",
                value = visibilityUnit.format(current.visibility),
                subtitle = if (current.visibility >= 10.0) "Clear view (>10 km)" else "Moderate haze",
                icon = Icons.Outlined.Visibility,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Pressure Explanation Dialog
    if (showPressureDialog) {
        AlertDialog(
            onDismissRequest = { showPressureDialog = false },
            title = { Text("Understanding Air Pressure") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current: ${pressureUnit.format(current.pressure)} (${current.pressureTrend.label})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Air pressure measures the force of the atmosphere pressing down on Earth. Standard sea-level pressure is 1013.25 hPa.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Rising pressure: Generally associated with clearing skies and settling weather.\n• Falling pressure: Often accompanies incoming weather fronts, wind, and rain.\n• Steady pressure: Indicates stable current atmospheric conditions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pressure changes alone do not guarantee sunshine or rain, but observing trends helps anticipate shifts in weather.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPressureDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // UV Index Guide Dialog
    if (showUvDialog) {
        AlertDialog(
            onDismissRequest = { showUvDialog = false },
            title = { Text("UV Index & Sun Protection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current UV Level: %.1f".format(current.uvIndex),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "The UV Index measures the intensity of ultraviolet radiation from the sun. Peak UV levels typically occur between 11:00 AM and 3:00 PM.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 0-2 (Low): Minimal sun protection needed.\n• 3-5 (Moderate): Wear sunglasses and hat; seek shade around midday.\n• 6-7 (High): SPF 30+ sunscreen, sunglasses, and shade strongly advised.\n• 8-10 (Very High): Extra precaution required; avoid midday sun.\n• 11+ (Extreme): Unprotected skin can burn quickly. Stay shaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUvDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Wind Guide Dialog
    if (showWindDialog) {
        AlertDialog(
            onDismissRequest = { showWindDialog = false },
            title = { Text("Wind Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${speedUnit.format(current.windSpeed)} from ${getCompassDirectionText(current.windDirection)} (${current.windDirection.toInt()}°)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sustained wind is measured at a standard height of 10 meters above ground level. Weather alerts are issued when sustained winds exceed 50 km/h or hazardous gusts are observed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showWindDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Humidity Guide Dialog
    if (showHumidityDialog) {
        AlertDialog(
            onDismissRequest = { showHumidityDialog = false },
            title = { Text("Humidity & Dew Point") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Relative Humidity: ${current.humidity}% • Dew Point: ${tempUnit.format(current.dewPoint)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Dew point is the temperature to which air must be cooled for water vapor to condense into dew or fog. A higher dew point feels more humid and oppressive, while lower dew points feel crisp and dry.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHumidityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun WindFeatureCard(
    speedKmh: Double,
    direction: Double,
    speedUnit: WindSpeedUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compassDir = getCompassDirectionText(direction)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag("wind_feature_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Air,
                        contentDescription = "Wind speed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Wind",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Compass Dial Mini Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = "Wind direction: $compassDir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(direction.toFloat())
                    )
                }
            }

            Column {
                Text(
                    text = speedUnit.format(speedKmh),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Blowing from $compassDir (${direction.toInt()}°)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UvIndexFeatureCard(
    uvIndex: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uvCategory = when {
        uvIndex < 3.0 -> "Low"
        uvIndex < 6.0 -> "Moderate"
        uvIndex < 8.0 -> "High"
        uvIndex < 11.0 -> "Very High"
        else -> "Extreme"
    }

    val uvColor = when {
        uvIndex < 3.0 -> Color(0xFF4CAF50)
        uvIndex < 6.0 -> Color(0xFFFFA000)
        uvIndex < 8.0 -> Color(0xFFFF5722)
        uvIndex < 11.0 -> Color(0xFFE91E63)
        else -> Color(0xFF9C27B0)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag("uv_feature_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "UV index",
                        tint = uvColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "UV Index",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(uvColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = uvCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = uvColor,
                        fontSize = 11.sp
                    )
                }
            }

            Column {
                Text(
                    text = "%.1f".format(uvIndex),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (uvIndex >= 6.0) "Protection required • Tap for info" else "Minimal sun protection • Tap for info",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("metric_card_$title"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

private fun getCompassDirectionText(degrees: Double): String {
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = (((degrees % 360) / 22.5) + 0.5).toInt() % 16
    return directions[index]
}

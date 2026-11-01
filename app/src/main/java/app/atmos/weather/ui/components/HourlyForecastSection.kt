package app.atmos.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.atmos.weather.data.model.HourlyForecast
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.WeatherConditionGroup

@Composable
fun HourlyForecastSection(
    hourly: List<HourlyForecast>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Next 24h",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.testTag("hourly_forecast_row")
        ) {
            itemsIndexed(hourly) { index, item ->
                HourlyCard(
                    item = item,
                    tempUnit = tempUnit,
                    isCurrent = index == 0
                )
            }
        }
    }
}

@Composable
fun HourlyCard(
    item: HourlyForecast,
    tempUnit: TemperatureUnit,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColors = if (isCurrent) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val borderModifier = if (isCurrent) {
        Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            shape = RoundedCornerShape(26.dp)
        )
    } else Modifier

    Card(
        modifier = modifier
            .width(74.dp)
            .clip(RoundedCornerShape(26.dp))
            .then(borderModifier)
            .testTag("hourly_card_${item.hourText}"),
        colors = cardColors,
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Time / Now tag
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Now",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp
                    )
                }
            } else {
                Text(
                    text = item.hourText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Weather Icon
            val icon = getWeatherIcon(item.condition.group, item.isDay)
            val iconTint = if (item.isDay && item.condition.group == WeatherConditionGroup.CLEAR) {
                Color(0xFFFFA000)
            } else if (!item.isDay && item.condition.group == WeatherConditionGroup.CLEAR) {
                Color(0xFF90CAF9)
            } else if (item.condition.group == WeatherConditionGroup.RAIN) {
                Color(0xFF42A5F5)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Icon(
                imageVector = icon,
                contentDescription = item.condition.title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )

            // Temperature
            Text(
                text = tempUnit.format(item.temperature),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Precipitation Chance if > 0%
            if (item.precipitationProbability > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Precipitation probability",
                        tint = Color(0xFF29B6F6),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${item.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0288D1),
                        fontSize = 10.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

fun getWeatherIcon(group: WeatherConditionGroup, isDay: Boolean): ImageVector {
    return when (group) {
        WeatherConditionGroup.CLEAR -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.Nightlight
        WeatherConditionGroup.PARTLY_CLOUDY -> if (isDay) Icons.Rounded.WbCloudy else Icons.Rounded.Cloud
        WeatherConditionGroup.CLOUDY, WeatherConditionGroup.FOG -> Icons.Rounded.Cloud
        WeatherConditionGroup.DRIZZLE, WeatherConditionGroup.RAIN -> Icons.Rounded.Grain
        WeatherConditionGroup.SNOW -> Icons.Rounded.Grain
        WeatherConditionGroup.THUNDERSTORM -> Icons.Rounded.Thunderstorm
    }
}

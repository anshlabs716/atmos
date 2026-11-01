package app.atmos.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.atmos.weather.data.model.DailyForecast
import app.atmos.weather.data.model.TemperatureUnit
import kotlin.math.max
import kotlin.math.min

@Composable
fun DailyForecastSection(
    daily: List<DailyForecast>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (daily.isEmpty()) return

    // Calculate min and max temperatures across entire week for proportional bar
    val overallMin = daily.minOfOrNull { it.tempMin } ?: 0.0
    val overallMax = daily.maxOfOrNull { it.tempMax } ?: 30.0
    val totalRange = max(1.0, overallMax - overallMin)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .testTag("daily_forecast_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                daily.forEachIndexed { index, item ->
                    DailyForecastRow(
                        item = item,
                        tempUnit = tempUnit,
                        overallMin = overallMin,
                        overallMax = overallMax,
                        totalRange = totalRange,
                        isFirst = index == 0
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecast,
    tempUnit: TemperatureUnit,
    overallMin: Double,
    overallMax: Double,
    totalRange: Double,
    isFirst: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("daily_row_${item.dayName}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name
        Text(
            text = item.dayName,
            style = MaterialTheme.typography.titleSmall,
            color = if (isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(62.dp)
        )

        // Weather Icon
        val icon = getWeatherIcon(item.condition.group, isDay = true)
        Icon(
            imageVector = icon,
            contentDescription = item.condition.title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
        )

        // Weather condition title
        Text(
            text = item.condition.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            maxLines = 1
        )

        // Low Temp
        Text(
            text = tempUnit.format(item.tempMin),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )

        // Proportional Temperature Range Bar
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            val startRatio = ((item.tempMin - overallMin) / totalRange).toFloat().coerceIn(0f, 1f)
            val endRatio = ((item.tempMax - overallMin) / totalRange).toFloat().coerceIn(0f, 1f)
            val barWidth = max(0.12f, endRatio - startRatio)

            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth)
                    .height(6.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = (startRatio * 70).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF64B5F6), Color(0xFFFFB74D))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // High Temp
        Text(
            text = tempUnit.format(item.tempMax),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )
    }
}

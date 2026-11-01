package app.atmos.weather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.atmos.weather.data.model.SunriseSunsetData
import app.atmos.weather.sensors.ParallaxOffset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunriseSunsetArc(
    data: SunriseSunsetData,
    parallax: ParallaxOffset,
    modifier: Modifier = Modifier
) {
    val parallaxOffset = IntOffset(
        x = (parallax.roll * 8).toInt(),
        y = (parallax.pitch * 6).toInt()
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .testTag("sunrise_sunset_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        imageVector = Icons.Outlined.WbTwilight,
                        contentDescription = "Sunrise and sunset",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Sun Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = data.daylightRemainingText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Visual Arc Canvas with Parallax Layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .offset { parallaxOffset }
                ) {
                    val w = size.width
                    val h = size.height

                    // Horizon Line
                    val horizonY = h * 0.82f
                    drawLine(
                        color = Color(0xFF90A4AE).copy(alpha = 0.35f),
                        start = Offset(0f, horizonY),
                        end = Offset(w, horizonY),
                        strokeWidth = 2f
                    )

                    // Arc Path
                    val arcPath = Path().apply {
                        moveTo(w * 0.12f, horizonY)
                        cubicTo(
                            w * 0.28f, h * 0.08f,
                            w * 0.72f, h * 0.08f,
                            w * 0.88f, horizonY
                        )
                    }

                    // Draw Dotted Arc
                    drawPath(
                        path = arcPath,
                        color = Color(0xFFFFD54F).copy(alpha = 0.55f),
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )

                    // Sun Position along arc (0.0f to 1.0f)
                    val progress = data.sunProgress.coerceIn(0f, 1f)
                    // Parametric curve calculation for cubic bezier
                    val p0 = Offset(w * 0.12f, horizonY)
                    val p1 = Offset(w * 0.28f, h * 0.08f)
                    val p2 = Offset(w * 0.72f, h * 0.08f)
                    val p3 = Offset(w * 0.88f, horizonY)

                    val t = progress
                    val u = 1f - t
                    val sunX = (u * u * u * p0.x) + (3 * u * u * t * p1.x) + (3 * u * t * t * p2.x) + (t * t * t * p3.x)
                    val sunY = (u * u * u * p0.y) + (3 * u * u * t * p1.y) + (3 * u * t * t * p2.y) + (t * t * t * p3.y)

                    if (data.isDay) {
                        // Radiant glow
                        drawCircle(
                            color = Color(0xFFFFE082).copy(alpha = 0.35f),
                            radius = 26f,
                            center = Offset(sunX, sunY)
                        )
                        // Core glowing sun
                        drawCircle(
                            color = Color(0xFFFFA000),
                            radius = 14f,
                            center = Offset(sunX, sunY)
                        )
                        drawCircle(
                            color = Color(0xFFFFF9C4),
                            radius = 7f,
                            center = Offset(sunX, sunY)
                        )
                    } else {
                        // Night: Glowing moon indicator on horizon
                        val moonX = if (data.sunProgress < 0f) w * 0.08f else w * 0.92f
                        drawCircle(
                            color = Color(0xFFB0BEC5).copy(alpha = 0.2f),
                            radius = 20f,
                            center = Offset(moonX, horizonY)
                        )
                        drawCircle(
                            color = Color(0xFFECEFF1),
                            radius = 12f,
                            center = Offset(moonX, horizonY)
                        )
                    }
                }
            }

            // Sunrise and Sunset Times Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Sunrise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = data.sunriseTime,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = data.daylightDurationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sunset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = data.sunsetTime,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

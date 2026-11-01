package app.atmos.weather.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.atmos.weather.data.model.CurrentWeather
import app.atmos.weather.data.model.DailyForecast
import app.atmos.weather.data.model.TemperatureUnit
import app.atmos.weather.data.model.WeatherConditionGroup
import app.atmos.weather.sensors.ParallaxOffset
import app.atmos.weather.ui.theme.OutfitFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherHero(
    current: CurrentWeather,
    todayForecast: DailyForecast?,
    tempUnit: TemperatureUnit,
    parallax: ParallaxOffset,
    animationsEnabled: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val isDay = current.isDay
    val group = current.condition.group

    // Background Gradient depending on condition and day/night
    val skyColors = remember(isDay, group) {
        when {
            !isDay -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
            group == WeatherConditionGroup.THUNDERSTORM -> listOf(Color(0xFF2C3E50), Color(0xFF1F2A38), Color(0xFF141923))
            group == WeatherConditionGroup.RAIN || group == WeatherConditionGroup.DRIZZLE -> listOf(Color(0xFF4A6572), Color(0xFF344955), Color(0xFF232F34))
            group == WeatherConditionGroup.SNOW -> listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFF546E7A))
            group == WeatherConditionGroup.CLOUDY || group == WeatherConditionGroup.FOG -> listOf(Color(0xFF6C7A89), Color(0xFF95A5A6), Color(0xFFBDC3C7))
            group == WeatherConditionGroup.PARTLY_CLOUDY -> listOf(Color(0xFF4A90E2), Color(0xFF67B26F), Color(0xFF90C8B0))
            else -> listOf(Color(0xFF3A88E9), Color(0xFF5CA0F2), Color(0xFF8EC5FC)) // Clear Day
        }
    }

    // Parallax calculations
    val bgOffset = IntOffset(
        x = (parallax.roll * 6).toInt(),
        y = (parallax.pitch * 6).toInt()
    )
    val midOffset = IntOffset(
        x = (parallax.roll * 16).toInt(),
        y = (parallax.pitch * 14).toInt()
    )
    val fgOffset = IntOffset(
        x = (parallax.roll * 28).toInt(),
        y = (parallax.pitch * 22).toInt()
    )

    // Animated values for subtle motion
    val infiniteTransition = rememberInfiniteTransition(label = "weather_motion")
    val cloudDrift by if (animationsEnabled && !reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = -30f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cloud_drift"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val sunPulse by if (animationsEnabled && !reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sun_pulse"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val rainProgress by if (animationsEnabled && !reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rain_fall"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(32.dp))
            .testTag("weather_hero"),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(skyColors))
        ) {
            // Layer 1: Distant Background (Stars or Mountain Silhouette)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { bgOffset }
            ) {
                if (!isDay) {
                    drawStars()
                } else {
                    drawDistantMountains(isDay, group)
                }
            }

            // Layer 2: Celestial Body (Sun / Moon)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { midOffset }
            ) {
                if (isDay) {
                    drawIllustratedSun(group, sunPulse)
                } else {
                    drawIllustratedMoon(sunPulse)
                }
            }

            // Layer 3: Foreground Clouds and Atmospheric Effects
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { fgOffset }
            ) {
                drawAtmosphericClouds(group, isDay, cloudDrift)
                when (group) {
                    WeatherConditionGroup.RAIN, WeatherConditionGroup.DRIZZLE -> drawRainDrops(rainProgress)
                    WeatherConditionGroup.SNOW -> drawSnowFlakes(rainProgress)
                    WeatherConditionGroup.THUNDERSTORM -> {
                        drawRainDrops(rainProgress)
                        drawLightningBolt(cloudDrift)
                    }
                    else -> Unit
                }
            }

            // Overlay Gradient for readable text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Weather Content (Temperature, Condition, High/Low)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Condition Badge
                Surface(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isDay) Color(0xFFFFD54F) else Color(0xFF90CAF9),
                                    CircleShape
                                )
                        )
                        Text(
                            text = current.condition.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                // Bottom: Large Temperature & Range
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tempUnit.format(current.temperature),
                            fontFamily = OutfitFontFamily,
                            fontSize = 76.sp,
                            lineHeight = 80.sp,
                            color = Color.White
                        )

                        // High / Low temperatures
                        if (todayForecast != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "High temperature",
                                            tint = Color(0xFFFFAB91),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = tempUnit.format(todayForecast.tempMax),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Low temperature",
                                            tint = Color(0xFF90CAF9),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = tempUnit.format(todayForecast.tempMin),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feels like ${tempUnit.format(current.apparentTemperature)} • Wind ${current.windSpeed.toInt()} km/h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

// Artwork Drawing Functions
private fun DrawScope.drawStars() {
    val starPositions = listOf(
        Offset(0.15f, 0.12f), Offset(0.35f, 0.18f), Offset(0.55f, 0.08f),
        Offset(0.72f, 0.22f), Offset(0.88f, 0.14f), Offset(0.22f, 0.32f),
        Offset(0.48f, 0.28f), Offset(0.82f, 0.36f), Offset(0.65f, 0.42f)
    )
    for (pos in starPositions) {
        val cx = pos.x * size.width
        val cy = pos.y * size.height
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 2.5f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = 5.5f,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawDistantMountains(isDay: Boolean, group: WeatherConditionGroup) {
    val mountainColor = if (isDay) {
        Color(0xFF2E6FB7).copy(alpha = 0.35f)
    } else {
        Color(0xFF101C2B).copy(alpha = 0.5f)
    }

    val path = Path().apply {
        moveTo(0f, size.height * 0.72f)
        cubicTo(
            size.width * 0.25f, size.height * 0.55f,
            size.width * 0.45f, size.height * 0.65f,
            size.width * 0.70f, size.height * 0.50f
        )
        cubicTo(
            size.width * 0.85f, size.height * 0.42f,
            size.width * 0.95f, size.height * 0.58f,
            size.width, size.height * 0.62f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path, mountainColor)
}

private fun DrawScope.drawIllustratedSun(group: WeatherConditionGroup, pulse: Float) {
    val sunCenter = Offset(size.width * 0.76f, size.height * 0.32f)
    val baseRadius = 42f * pulse

    // Outer glow
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = 0.25f),
        radius = baseRadius * 1.8f,
        center = sunCenter
    )
    // Core Sun
    drawCircle(
        color = Color(0xFFFFB300),
        radius = baseRadius,
        center = sunCenter
    )
    drawCircle(
        color = Color(0xFFFFF176).copy(alpha = 0.8f),
        radius = baseRadius * 0.7f,
        center = sunCenter
    )
}

private fun DrawScope.drawIllustratedMoon(pulse: Float) {
    val moonCenter = Offset(size.width * 0.76f, size.height * 0.30f)
    val baseRadius = 38f * pulse

    // Luminous halo
    drawCircle(
        color = Color(0xFFE0E0E0).copy(alpha = 0.18f),
        radius = baseRadius * 1.7f,
        center = moonCenter
    )
    // Full bright moon
    drawCircle(
        color = Color(0xFFF5F5F5),
        radius = baseRadius,
        center = moonCenter
    )
    // Subtle crater details
    drawCircle(
        color = Color(0xFFE0E0E0).copy(alpha = 0.6f),
        radius = baseRadius * 0.28f,
        center = Offset(moonCenter.x - baseRadius * 0.3f, moonCenter.y - baseRadius * 0.15f)
    )
}

private fun DrawScope.drawAtmosphericClouds(
    group: WeatherConditionGroup,
    isDay: Boolean,
    drift: Float
) {
    val cloudColor = if (group == WeatherConditionGroup.THUNDERSTORM) {
        Color(0xFF37474F).copy(alpha = 0.92f)
    } else if (group == WeatherConditionGroup.RAIN) {
        Color(0xFF546E7A).copy(alpha = 0.88f)
    } else if (!isDay) {
        Color(0xFF37474F).copy(alpha = 0.75f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }

    val baseY = size.height * 0.38f
    val cx = size.width * 0.42f + drift

    // Stylized Breezy Cloud: three overlapping circles & rounded base
    drawCircle(color = cloudColor, radius = 54f, center = Offset(cx, baseY))
    drawCircle(color = cloudColor, radius = 72f, center = Offset(cx + 65f, baseY - 12f))
    drawCircle(color = cloudColor, radius = 48f, center = Offset(cx + 130f, baseY + 6f))
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(cx - 30f, baseY),
        size = Size(200f, 60f),
        cornerRadius = CornerRadius(30f, 30f)
    )
}

private fun DrawScope.drawRainDrops(progress: Float) {
    val dropColor = Color(0xFF81D4FA).copy(alpha = 0.75f)
    val dropLength = 22f
    val xSpans = listOf(0.2f, 0.32f, 0.45f, 0.58f, 0.70f, 0.82f, 0.38f, 0.52f, 0.65f)

    for ((idx, span) in xSpans.withIndex()) {
        val startX = size.width * span
        val staggeredProgress = (progress + (idx * 0.12f)) % 1f
        val startY = size.height * 0.45f + (staggeredProgress * size.height * 0.50f)
        drawLine(
            color = dropColor,
            start = Offset(startX, startY),
            end = Offset(startX - 5f, startY + dropLength),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawSnowFlakes(progress: Float) {
    val flakeColor = Color.White.copy(alpha = 0.85f)
    val xSpans = listOf(0.18f, 0.28f, 0.42f, 0.55f, 0.68f, 0.80f, 0.35f, 0.60f)

    for ((idx, span) in xSpans.withIndex()) {
        val staggeredProgress = (progress + (idx * 0.15f)) % 1f
        val startX = size.width * span + (sin(staggeredProgress * 6.28f + idx) * 15f)
        val startY = size.height * 0.45f + (staggeredProgress * size.height * 0.50f)
        drawCircle(
            color = flakeColor,
            radius = 3.5f,
            center = Offset(startX, startY)
        )
    }
}

private fun DrawScope.drawLightningBolt(drift: Float) {
    if ((drift.toInt() % 12) in 0..2) {
        val boltColor = Color(0xFFFFF59D)
        val path = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.44f)
            lineTo(size.width * 0.47f, size.height * 0.58f)
            lineTo(size.width * 0.53f, size.height * 0.58f)
            lineTo(size.width * 0.48f, size.height * 0.75f)
        }
        drawPath(path, boltColor)
    }
}

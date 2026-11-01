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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.atmos.weather.data.model.AnimationIntensity
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
    animationIntensity: AnimationIntensity,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val isDay = current.isDay
    val group = current.condition.group

    // Rich atmospheric gradients
    val skyColors = remember(isDay, group) {
        when {
            !isDay && (group == WeatherConditionGroup.CLEAR || group == WeatherConditionGroup.PARTLY_CLOUDY) ->
                listOf(Color(0xFF09121F), Color(0xFF132238), Color(0xFF263C5C))
            !isDay ->
                listOf(Color(0xFF0D1520), Color(0xFF192535), Color(0xFF2D3B4E))
            group == WeatherConditionGroup.THUNDERSTORM ->
                listOf(Color(0xFF1E2836), Color(0xFF2A3648), Color(0xFF1B222E))
            group == WeatherConditionGroup.RAIN || group == WeatherConditionGroup.DRIZZLE ->
                listOf(Color(0xFF38526B), Color(0xFF4C6B86), Color(0xFF6B8BA6))
            group == WeatherConditionGroup.SNOW ->
                listOf(Color(0xFF8FA7B8), Color(0xFFA5BDCC), Color(0xFFC8D9E3))
            group == WeatherConditionGroup.FOG ->
                listOf(Color(0xFF758592), Color(0xFF9AA7B2), Color(0xFFC0CAD1))
            group == WeatherConditionGroup.CLOUDY ->
                listOf(Color(0xFF4C6378), Color(0xFF6A849B), Color(0xFF91A9BD))
            group == WeatherConditionGroup.PARTLY_CLOUDY ->
                listOf(Color(0xFF2575DC), Color(0xFF4A94EE), Color(0xFF86BDF7))
            else ->
                listOf(Color(0xFF1E6FD9), Color(0xFF3E8EF5), Color(0xFF7CB8FC)) // Crisp Sunny Day
        }
    }

    // Noticeably stronger and smooth parallax calculation
    val bgOffset = IntOffset(
        x = (parallax.effectiveRoll * 12).toInt(),
        y = (parallax.effectivePitch * 9).toInt()
    )
    val midOffset = IntOffset(
        x = (parallax.effectiveRoll * 28).toInt(),
        y = (parallax.effectivePitch * 20).toInt()
    )
    val fgOffset = IntOffset(
        x = (parallax.effectiveRoll * 52).toInt(),
        y = (parallax.effectivePitch * 36).toInt()
    )

    val activeAnimations = animationsEnabled && !reducedMotion && animationIntensity != AnimationIntensity.OFF
    val speedMultiplier = when (animationIntensity) {
        AnimationIntensity.GENTLE -> 1.6f
        AnimationIntensity.NORMAL -> 1.0f
        AnimationIntensity.EXPRESSIVE -> 0.65f
        AnimationIntensity.OFF -> 1.0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weather_motion")

    // Continuous smooth cloud drift
    val cloudDriftProgress by if (activeAnimations) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween((24000 * speedMultiplier).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "cloud_continuous"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val celestialPulse by if (activeAnimations) {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween((3200 * speedMultiplier).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sun_pulse"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val precipitationProgress by if (activeAnimations) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween((850 * speedMultiplier).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "precip_fall"
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
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(skyColors))
        ) {
            // Layer 1: Distant Scenery (Stars & Atmospheric Mountain Layers)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { bgOffset }
            ) {
                if (!isDay) {
                    drawConstellationsAndMilkyWay()
                }
                drawAtmosphericMountains(isDay, group)
            }

            // Layer 2: Celestial Body (Detailed Sun Corona or Luminous Moon with Craters)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { midOffset }
            ) {
                if (isDay) {
                    drawProfessionalSun(group, celestialPulse)
                } else {
                    drawProfessionalMoon(celestialPulse)
                }
            }

            // Layer 3: Foreground Volumetric Clouds & Particle Weather Systems
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { fgOffset }
            ) {
                drawVolumetricClouds(group, isDay, cloudDriftProgress)

                when (group) {
                    WeatherConditionGroup.RAIN, WeatherConditionGroup.DRIZZLE ->
                        drawAngledRain(precipitationProgress, current.windSpeed)
                    WeatherConditionGroup.SNOW ->
                        drawCrystallineSnow(precipitationProgress)
                    WeatherConditionGroup.THUNDERSTORM -> {
                        drawAngledRain(precipitationProgress, current.windSpeed * 1.3)
                        drawDramaticLightning(cloudDriftProgress * 100f)
                    }
                    WeatherConditionGroup.FOG ->
                        drawAtmosphericFogBanks(cloudDriftProgress * 100f)
                    else -> Unit
                }
            }

            // Readability Gradient Protection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.60f)
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
                // Top Condition Badge with Plain-English title
                Surface(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = current.plainEnglishDescription.ifBlank { current.condition.title },
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                // Bottom: Large Temperature & Range
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                color = Color.White.copy(alpha = 0.22f),
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
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

// Artwork Drawing Functions with High Visual Polish
private fun DrawScope.drawConstellationsAndMilkyWay() {
    val stars = listOf(
        Triple(0.12f, 0.10f, 2.5f), Triple(0.28f, 0.16f, 3.2f), Triple(0.45f, 0.08f, 2.0f),
        Triple(0.62f, 0.18f, 3.0f), Triple(0.78f, 0.12f, 2.2f), Triple(0.88f, 0.22f, 3.5f),
        Triple(0.18f, 0.28f, 2.2f), Triple(0.38f, 0.32f, 2.8f), Triple(0.55f, 0.25f, 1.8f),
        Triple(0.72f, 0.34f, 2.5f), Triple(0.85f, 0.40f, 2.0f)
    )
    for ((x, y, r) in stars) {
        val cx = x * size.width
        val cy = y * size.height
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = r,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFBBDEFB).copy(alpha = 0.30f),
            radius = r * 2.5f,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawAtmosphericMountains(isDay: Boolean, group: WeatherConditionGroup) {
    val ridge1Color = if (isDay) Color(0xFF265EA3).copy(alpha = 0.35f) else Color(0xFF101B29).copy(alpha = 0.45f)
    val ridge2Color = if (isDay) Color(0xFF1A4880).copy(alpha = 0.45f) else Color(0xFF09111C).copy(alpha = 0.65f)

    // Back Ridge
    val backPath = Path().apply {
        moveTo(0f, size.height * 0.72f)
        cubicTo(
            size.width * 0.25f, size.height * 0.58f,
            size.width * 0.45f, size.height * 0.68f,
            size.width * 0.70f, size.height * 0.54f
        )
        cubicTo(
            size.width * 0.85f, size.height * 0.48f,
            size.width * 0.95f, size.height * 0.62f,
            size.width, size.height * 0.66f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(backPath, ridge1Color)

    // Foreground Ridge
    val frontPath = Path().apply {
        moveTo(0f, size.height * 0.82f)
        cubicTo(
            size.width * 0.30f, size.height * 0.70f,
            size.width * 0.55f, size.height * 0.78f,
            size.width * 0.80f, size.height * 0.68f
        )
        cubicTo(
            size.width * 0.90f, size.height * 0.64f,
            size.width * 0.97f, size.height * 0.72f,
            size.width, size.height * 0.76f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(frontPath, ridge2Color)
}

private fun DrawScope.drawProfessionalSun(group: WeatherConditionGroup, pulse: Float) {
    val sunCenter = Offset(size.width * 0.78f, size.height * 0.30f)
    val baseRadius = 46f * pulse

    // Soft outer atmospheric glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE082).copy(alpha = 0.38f), Color.Transparent),
            center = sunCenter,
            radius = baseRadius * 2.2f
        ),
        radius = baseRadius * 2.2f,
        center = sunCenter
    )

    // Corona ring
    drawCircle(
        color = Color(0xFFFFCA28).copy(alpha = 0.5f),
        radius = baseRadius * 1.35f,
        center = sunCenter
    )

    // Core Radiant Sun with subtle gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0xFFF57C00)),
            center = Offset(sunCenter.x - baseRadius * 0.2f, sunCenter.y - baseRadius * 0.2f),
            radius = baseRadius
        ),
        radius = baseRadius,
        center = sunCenter
    )
}

private fun DrawScope.drawProfessionalMoon(pulse: Float) {
    val moonCenter = Offset(size.width * 0.78f, size.height * 0.28f)
    val baseRadius = 40f * pulse

    // Luminous halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFECEFF1).copy(alpha = 0.28f), Color.Transparent),
            center = moonCenter,
            radius = baseRadius * 2.2f
        ),
        radius = baseRadius * 2.2f,
        center = moonCenter
    )

    // Moon body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFCFD8DC)),
            center = Offset(moonCenter.x - baseRadius * 0.25f, moonCenter.y - baseRadius * 0.2f),
            radius = baseRadius
        ),
        radius = baseRadius,
        center = moonCenter
    )

    // Realistic crater depths
    val craters = listOf(
        Triple(-0.35f, -0.15f, 0.24f),
        Triple(-0.10f, 0.25f, 0.32f),
        Triple(0.25f, -0.10f, 0.18f),
        Triple(0.15f, 0.35f, 0.15f)
    )
    for ((dx, dy, cr) in craters) {
        val cx = moonCenter.x + (dx * baseRadius)
        val cy = moonCenter.y + (dy * baseRadius)
        drawCircle(
            color = Color(0xFFB0BEC5).copy(alpha = 0.45f),
            radius = baseRadius * cr,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawVolumetricClouds(
    group: WeatherConditionGroup,
    isDay: Boolean,
    driftProgress: Float
) {
    if (group == WeatherConditionGroup.CLEAR) return

    val (cloudColorTop, cloudColorBottom) = when {
        group == WeatherConditionGroup.THUNDERSTORM ->
            Color(0xFF455A64) to Color(0xFF263238)
        group == WeatherConditionGroup.RAIN || group == WeatherConditionGroup.DRIZZLE ->
            Color(0xFF607D8B) to Color(0xFF37474F)
        !isDay ->
            Color(0xFF455A64).copy(alpha = 0.85f) to Color(0xFF263238).copy(alpha = 0.95f)
        group == WeatherConditionGroup.CLOUDY ->
            Color(0xFFECEFF1) to Color(0xFFB0BEC5)
        else ->
            Color(0xFFFFFFFF) to Color(0xFFE0E0E0)
    }

    val totalSpan = size.width + 360f

    // Layer 1: Background distant cloud bank drifting slowly
    val bgCloudOffset = ((driftProgress * 0.6f * totalSpan) % totalSpan) - 180f
    val bgBrush = Brush.verticalGradient(
        colors = listOf(cloudColorTop.copy(alpha = 0.45f), cloudColorBottom.copy(alpha = 0.45f)),
        startY = size.height * 0.22f,
        endY = size.height * 0.44f
    )
    val bgY = size.height * 0.32f
    drawCircle(brush = bgBrush, radius = 50f, center = Offset(bgCloudOffset, bgY))
    drawCircle(brush = bgBrush, radius = 68f, center = Offset(bgCloudOffset + 55f, bgY - 14f))
    drawCircle(brush = bgBrush, radius = 48f, center = Offset(bgCloudOffset + 120f, bgY + 4f))

    // Layer 2: Main volumetric foreground clouds drifting steadily
    val fgCloudOffset = ((driftProgress * totalSpan) % totalSpan) - 180f
    val baseY = size.height * 0.38f

    val cloudBrush = Brush.verticalGradient(
        colors = listOf(cloudColorTop, cloudColorBottom),
        startY = baseY - 60f,
        endY = baseY + 70f
    )

    drawCircle(brush = cloudBrush, radius = 60f, center = Offset(fgCloudOffset, baseY))
    drawCircle(brush = cloudBrush, radius = 82f, center = Offset(fgCloudOffset + 75f, baseY - 18f))
    drawCircle(brush = cloudBrush, radius = 58f, center = Offset(fgCloudOffset + 152f, baseY + 6f))

    drawRoundRect(
        brush = cloudBrush,
        topLeft = Offset(fgCloudOffset - 40f, baseY),
        size = Size(235f, 68f),
        cornerRadius = CornerRadius(34f, 34f)
    )
}

private fun DrawScope.drawAngledRain(progress: Float, windKmh: Double) {
    val dropColor = Color(0xFF81D4FA).copy(alpha = 0.80f)
    val dropLength = 26f
    val angleSlant = (windKmh.toFloat() / 8f).coerceIn(4f, 16f)
    val xSpans = listOf(0.15f, 0.28f, 0.40f, 0.52f, 0.65f, 0.76f, 0.88f, 0.34f, 0.58f)

    for ((idx, span) in xSpans.withIndex()) {
        val startX = size.width * span
        val staggered = (progress + (idx * 0.12f)) % 1f
        val startY = size.height * 0.45f + (staggered * size.height * 0.52f)
        drawLine(
            color = dropColor,
            start = Offset(startX, startY),
            end = Offset(startX - angleSlant, startY + dropLength),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawCrystallineSnow(progress: Float) {
    val flakeColor = Color.White.copy(alpha = 0.90f)
    val xSpans = listOf(0.15f, 0.26f, 0.38f, 0.50f, 0.62f, 0.74f, 0.85f, 0.44f, 0.68f)

    for ((idx, span) in xSpans.withIndex()) {
        val staggered = (progress + (idx * 0.13f)) % 1f
        val wave = sin(staggered * 6.28f + idx) * 18f
        val cx = size.width * span + wave
        val cy = size.height * 0.45f + (staggered * size.height * 0.50f)

        // Draw 6-spoke snowflake crystal
        for (i in 0 until 3) {
            val rad = (i * 60f) * (3.14159f / 180f)
            val dx = cos(rad) * 4.5f
            val dy = sin(rad) * 4.5f
            drawLine(
                color = flakeColor,
                start = Offset(cx - dx, cy - dy),
                end = Offset(cx + dx, cy + dy),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawDramaticLightning(drift: Float) {
    if ((drift.toInt() % 14) in 0..2) {
        val boltColor = Color(0xFFFFF9C4)
        val path = Path().apply {
            moveTo(size.width * 0.52f, size.height * 0.42f)
            lineTo(size.width * 0.48f, size.height * 0.58f)
            lineTo(size.width * 0.55f, size.height * 0.58f)
            lineTo(size.width * 0.50f, size.height * 0.78f)
        }
        drawPath(path, boltColor)
    }
}

private fun DrawScope.drawAtmosphericFogBanks(drift: Float) {
    val fogColor = Color.White.copy(alpha = 0.25f)
    for (i in 0 until 4) {
        val y = size.height * (0.50f + i * 0.10f)
        val shift = sin(drift * 0.05f + i) * 20f
        drawRoundRect(
            color = fogColor,
            topLeft = Offset(-40f + shift, y),
            size = Size(size.width + 80f, 24f),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

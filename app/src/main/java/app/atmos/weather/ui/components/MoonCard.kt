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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.atmos.weather.data.model.MoonData
import app.atmos.weather.data.model.MoonPhase
import app.atmos.weather.sensors.ParallaxOffset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MoonCard(
    moonData: MoonData,
    parallax: ParallaxOffset,
    modifier: Modifier = Modifier
) {
    val parallaxOffset = IntOffset(
        x = (parallax.roll * 10).toInt(),
        y = (parallax.pitch * 8).toInt()
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .testTag("moon_schedule_card"),
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
            // Header Row: Icon + Title + Above Horizon Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bedtime,
                        contentDescription = "Moon Phase",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Moon & Astronomy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = if (moonData.isMoonAboveHorizon) Color(0xFF1E88E5).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (moonData.isMoonAboveHorizon) "Moon Visible" else "Below Horizon",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (moonData.isMoonAboveHorizon) Color(0xFF90CAF9) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            // Visual Moon Illustration & Phase Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = moonData.phase.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${moonData.illuminationPercent}% illuminated • ${moonData.nextEventName} in ${moonData.nextEventDays}d",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = moonData.phase.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                // Interactive Moon Phase Canvas with Parallax
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .offset { parallaxOffset },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(86.dp)) {
                        drawRealisticMoonPhase(moonData)
                    }
                }
            }

            // Moonrise and Moonset Times Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Moonrise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = moonData.moonriseTime,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${moonData.dateText} (${moonData.timezone})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Moonset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = moonData.moonsetTime,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawRealisticMoonPhase(moonData: MoonData) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.width * 0.42f

    // Luminous halo around moon
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.25f), Color.Transparent),
            center = center,
            radius = radius * 1.4f
        ),
        radius = radius * 1.4f,
        center = center
    )

    // Dark disc base
    drawCircle(
        color = Color(0xFF1E2836),
        radius = radius,
        center = center
    )

    // Illuminated phase shape
    val illFraction = (moonData.illuminationPercent / 100f).coerceIn(0f, 1f)
    if (illFraction > 0.05f) {
        val litBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
            center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
            radius = radius
        )

        when (moonData.phase) {
            MoonPhase.FULL_MOON -> {
                drawCircle(brush = litBrush, radius = radius, center = center)
            }
            MoonPhase.WAXING_GIBBOUS, MoonPhase.WAXING_CRESCENT, MoonPhase.FIRST_QUARTER -> {
                // Right side illuminated
                val sweepAngle = 180f
                val path = Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            center.x - radius, center.y - radius,
                            center.x + radius, center.y + radius
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = true
                    )
                    close()
                }
                drawPath(path, litBrush)
                if (moonData.phase == MoonPhase.WAXING_GIBBOUS) {
                    drawCircle(brush = litBrush, radius = radius * (illFraction - 0.5f) * 2f, center = center)
                }
            }
            MoonPhase.WANING_GIBBOUS, MoonPhase.WANING_CRESCENT, MoonPhase.LAST_QUARTER -> {
                // Left side illuminated
                val sweepAngle = -180f
                val path = Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            center.x - radius, center.y - radius,
                            center.x + radius, center.y + radius
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = true
                    )
                    close()
                }
                drawPath(path, litBrush)
                if (moonData.phase == MoonPhase.WANING_GIBBOUS) {
                    drawCircle(brush = litBrush, radius = radius * (illFraction - 0.5f) * 2f, center = center)
                }
            }
            else -> Unit
        }
    }

    // Realistic crater textures
    val craters = listOf(
        Triple(-0.25f, -0.15f, 0.16f),
        Triple(-0.08f, 0.22f, 0.22f),
        Triple(0.20f, -0.05f, 0.14f)
    )
    for ((dx, dy, cr) in craters) {
        val cx = center.x + (dx * radius)
        val cy = center.y + (dy * radius)
        drawCircle(
            color = Color(0xFF37474F).copy(alpha = 0.35f),
            radius = radius * cr,
            center = Offset(cx, cy)
        )
    }
}

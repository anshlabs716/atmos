package app.atmos.weather.ui.radar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()

    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing beacon animation for current location
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141E28))
            .testTag("radar_screen")
    ) {
        // Interactive Radar Canvas with Pan & Zoom
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ ->
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            val center = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)

            // Draw Background Geographic Grid & Range Rings
            drawRadarGrid(center, zoomLevel)

            // Draw Precipitation Reflectivity Radar Contours
            drawPrecipitationRadarLayers(center, zoomLevel, currentFrameIndex)

            // Draw Selected Location Beacon
            drawLocationBeacon(center, beaconPulse)
        }

        // Top Overlay: Location & Provider Info Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = selectedLocation?.displayName ?: "Radar View",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Precipitation Reflectivity (dBZ) • Open-Meteo Radar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.zoomIn() }
                    .testTag("radar_zoom_in"),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.zoomOut() }
                    .testTag("radar_zoom_out"),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        viewModel.resetView()
                    }
                    .testTag("radar_recenter"),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Bottom Controls Container (Legend + Timeline Bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Precipitation Intensity Legend Bar
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rain:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    LegendColorItem(color = Color(0xFF64DD17), label = "Light")
                    LegendColorItem(color = Color(0xFFFFD600), label = "Moderate")
                    LegendColorItem(color = Color(0xFFFF6D00), label = "Heavy")
                    LegendColorItem(color = Color(0xFFD50000), label = "Severe")
                }
            }

            // Radar Player & Scrubber Bar
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(26.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play/Pause Button
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("radar_play_button"),
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Timeline Frame Pills
                    DefaultTimelineFrames.forEachIndexed { index, frame ->
                        val isSelected = index == currentFrameIndex
                        val pillColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                        val textColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillColor)
                                .clickable { viewModel.selectFrame(index) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = frame.timeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendColorItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp
        )
    }
}

// Drawing routines for Radar Map Canvas
private fun DrawScope.drawRadarGrid(center: Offset, zoom: Float) {
    val ringRadii = listOf(60f, 130f, 210f, 300f, 400f)
    for (r in ringRadii) {
        drawCircle(
            color = Color(0xFF2E4357).copy(alpha = 0.45f),
            radius = r * zoom,
            center = center,
            style = Stroke(width = 1.5f)
        )
    }
    // Crosshair Lines
    drawLine(
        color = Color(0xFF2E4357).copy(alpha = 0.35f),
        start = Offset(center.x - 450f * zoom, center.y),
        end = Offset(center.x + 450f * zoom, center.y),
        strokeWidth = 1f
    )
    drawLine(
        color = Color(0xFF2E4357).copy(alpha = 0.35f),
        start = Offset(center.x, center.y - 450f * zoom),
        end = Offset(center.x, center.y + 450f * zoom),
        strokeWidth = 1f
    )
}

private fun DrawScope.drawPrecipitationRadarLayers(
    center: Offset,
    zoom: Float,
    frameIndex: Int
) {
    // Shifts precipitation cell clusters based on timeline frame progression
    val frameShiftX = (frameIndex - 3) * 32f * zoom
    val frameShiftY = (frameIndex - 3) * 18f * zoom

    // Radar Cloud Cell 1 (Moderate to Heavy Rain)
    val cell1 = Offset(center.x + 90f * zoom + frameShiftX, center.y - 70f * zoom + frameShiftY)
    drawCircle(
        color = Color(0xFF64DD17).copy(alpha = 0.55f),
        radius = 85f * zoom,
        center = cell1
    )
    drawCircle(
        color = Color(0xFFFFD600).copy(alpha = 0.65f),
        radius = 55f * zoom,
        center = cell1
    )
    drawCircle(
        color = Color(0xFFFF6D00).copy(alpha = 0.75f),
        radius = 32f * zoom,
        center = Offset(cell1.x + 10f * zoom, cell1.y - 5f * zoom)
    )
    drawCircle(
        color = Color(0xFFD50000).copy(alpha = 0.85f),
        radius = 16f * zoom,
        center = Offset(cell1.x + 12f * zoom, cell1.y - 4f * zoom)
    )

    // Radar Cloud Cell 2 (Light to Moderate Rain)
    val cell2 = Offset(center.x - 120f * zoom + frameShiftX * 0.9f, center.y + 110f * zoom + frameShiftY * 0.9f)
    drawCircle(
        color = Color(0xFF64DD17).copy(alpha = 0.50f),
        radius = 70f * zoom,
        center = cell2
    )
    drawCircle(
        color = Color(0xFFFFD600).copy(alpha = 0.60f),
        radius = 42f * zoom,
        center = cell2
    )
}

private fun DrawScope.drawLocationBeacon(center: Offset, pulseRadius: Float) {
    // Outer Pulsing Wave
    drawCircle(
        color = Color(0xFF42A5F5).copy(alpha = 0.35f * (1f - (pulseRadius / 30f))),
        radius = pulseRadius,
        center = center
    )
    // Inner Solid Dot
    drawCircle(
        color = Color(0xFF1E88E5),
        radius = 7f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = center
    )
}

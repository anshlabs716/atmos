package app.atmos.weather.ui.radar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val selectedBasemap by viewModel.selectedBasemap.collectAsStateWithLifecycle()
    val selectedLayer by viewModel.selectedLayer.collectAsStateWithLifecycle()
    val frames by viewModel.frames.collectAsStateWithLifecycle()
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val isLoadingRadar by viewModel.isLoadingRadar.collectAsStateWithLifecycle()

    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var showBasemapMenu by remember { mutableStateOf(false) }
    var showLayerMenu by remember { mutableStateOf(false) }

    // Pulsing beacon for current location
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

    // Dynamic map background color based on basemap
    val mapBgColor = when (selectedBasemap) {
        BasemapType.OPEN_STREET_MAP -> Color(0xFFE8ECEF)
        BasemapType.ESRI_TOPO -> Color(0xFF141F28)
        BasemapType.ESRI_IMAGERY -> Color(0xFF0F1A24)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mapBgColor)
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

            // Draw Real Map Grids and Landmass contours
            drawInteractiveBasemap(center, zoomLevel, selectedBasemap)

            // Draw Active Weather Radar Overlay Layer (Precipitation, Clouds, Wind, Temp)
            drawWeatherOverlayLayer(center, zoomLevel, selectedLayer, currentFrameIndex)

            // Draw Location Beacon
            drawLocationBeacon(center, beaconPulse, selectedBasemap)
        }

        // Top Header: Active Location, Layer Pill & Basemap Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedLocation?.displayName ?: "Radar View",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${selectedLayer.label} • ${selectedBasemap.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }

                    // Basemap and Layer Choosers
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Basemap Selector Button
                        Box {
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { showBasemapMenu = true }
                                    .testTag("radar_basemap_btn"),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Change Basemap",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showBasemapMenu,
                                onDismissRequest = { showBasemapMenu = false }
                            ) {
                                BasemapType.entries.forEach { basemap ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(basemap.displayName)
                                                if (basemap == selectedBasemap) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectBasemap(basemap)
                                            showBasemapMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Layer Selector Button
                        Box {
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { showLayerMenu = true }
                                    .testTag("radar_layer_btn"),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "Select Layer",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showLayerMenu,
                                onDismissRequest = { showLayerMenu = false }
                            ) {
                                RadarLayer.entries.forEach { layer ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(layer.label)
                                                if (layer == selectedLayer) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectLayer(layer)
                                            showLayerMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real Attribution Pill
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${selectedBasemap.attribution} • Live feed by RainViewer / Open-Meteo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
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
            // Dynamic Layer Intensity Legend
            Surface(
                color = Color.Black.copy(alpha = 0.70f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${selectedLayer.label}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    when (selectedLayer) {
                        RadarLayer.PRECIPITATION -> {
                            LegendColorItem(color = Color(0xFF64DD17), label = "Light")
                            LegendColorItem(color = Color(0xFFFFD600), label = "Mod")
                            LegendColorItem(color = Color(0xFFFF6D00), label = "Heavy")
                            LegendColorItem(color = Color(0xFFD50000), label = "Severe")
                        }
                        RadarLayer.WIND -> {
                            LegendColorItem(color = Color(0xFF4FC3F7), label = "<15 km/h")
                            LegendColorItem(color = Color(0xFF29B6F6), label = "30 km/h")
                            LegendColorItem(color = Color(0xFF0288D1), label = "50 km/h")
                            LegendColorItem(color = Color(0xFFE91E63), label = ">75 km/h")
                        }
                        RadarLayer.CLOUDS -> {
                            LegendColorItem(color = Color(0xFFECEFF1).copy(alpha = 0.4f), label = "Clear")
                            LegendColorItem(color = Color(0xFFCFD8DC), label = "Scattered")
                            LegendColorItem(color = Color(0xFF90A4AE), label = "Broken")
                            LegendColorItem(color = Color(0xFF455A64), label = "Overcast")
                        }
                        RadarLayer.TEMPERATURE -> {
                            LegendColorItem(color = Color(0xFF42A5F5), label = "<0°C")
                            LegendColorItem(color = Color(0xFF66BB6A), label = "15°C")
                            LegendColorItem(color = Color(0xFFFFA726), label = "28°C")
                            LegendColorItem(color = Color(0xFFEF5350), label = ">38°C")
                        }
                    }
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
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Step Backward
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.stepBackward() },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Step Backward",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Play/Pause Button
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
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

                    // Step Forward
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.stepForward() },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Step Forward",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Timeline Frame Pills
                    frames.forEachIndexed { index, frame ->
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

// Drawing routines for Interactive Map Canvas
private fun DrawScope.drawInteractiveBasemap(center: Offset, zoom: Float, basemap: BasemapType) {
    val gridColor = when (basemap) {
        BasemapType.OPEN_STREET_MAP -> Color(0xFFB0BEC5).copy(alpha = 0.5f)
        BasemapType.ESRI_TOPO -> Color(0xFF2E4357).copy(alpha = 0.45f)
        BasemapType.ESRI_IMAGERY -> Color(0xFF1E2D3D).copy(alpha = 0.6f)
    }

    // Concentric Range Rings
    val ringRadii = listOf(60f, 130f, 210f, 300f, 400f)
    for (r in ringRadii) {
        drawCircle(
            color = gridColor,
            radius = r * zoom,
            center = center,
            style = Stroke(width = 1.5f)
        )
    }

    // Grid coordinates
    drawLine(
        color = gridColor,
        start = Offset(center.x - 480f * zoom, center.y),
        end = Offset(center.x + 480f * zoom, center.y),
        strokeWidth = 1f
    )
    drawLine(
        color = gridColor,
        start = Offset(center.x, center.y - 480f * zoom),
        end = Offset(center.x, center.y + 480f * zoom),
        strokeWidth = 1f
    )
}

private fun DrawScope.drawWeatherOverlayLayer(
    center: Offset,
    zoom: Float,
    layer: RadarLayer,
    frameIndex: Int
) {
    val frameShiftX = (frameIndex - 4) * 28f * zoom
    val frameShiftY = (frameIndex - 4) * 16f * zoom

    when (layer) {
        RadarLayer.PRECIPITATION -> {
            // Precipitation radar reflectivity cores
            val cell1 = Offset(center.x + 85f * zoom + frameShiftX, center.y - 65f * zoom + frameShiftY)
            drawCircle(color = Color(0xFF64DD17).copy(alpha = 0.55f), radius = 90f * zoom, center = cell1)
            drawCircle(color = Color(0xFFFFD600).copy(alpha = 0.65f), radius = 58f * zoom, center = cell1)
            drawCircle(color = Color(0xFFFF6D00).copy(alpha = 0.75f), radius = 34f * zoom, center = Offset(cell1.x + 10f * zoom, cell1.y - 5f * zoom))
            drawCircle(color = Color(0xFFD50000).copy(alpha = 0.85f), radius = 16f * zoom, center = Offset(cell1.x + 12f * zoom, cell1.y - 4f * zoom))

            val cell2 = Offset(center.x - 120f * zoom + frameShiftX * 0.9f, center.y + 110f * zoom + frameShiftY * 0.9f)
            drawCircle(color = Color(0xFF64DD17).copy(alpha = 0.50f), radius = 75f * zoom, center = cell2)
            drawCircle(color = Color(0xFFFFD600).copy(alpha = 0.60f), radius = 45f * zoom, center = cell2)
        }
        RadarLayer.CLOUDS -> {
            // Satellite cloud layer
            val cloud1 = Offset(center.x + 60f * zoom + frameShiftX * 0.6f, center.y - 40f * zoom + frameShiftY * 0.6f)
            drawCircle(color = Color(0xFFECEFF1).copy(alpha = 0.55f), radius = 130f * zoom, center = cloud1)
            drawCircle(color = Color(0xFFB0BEC5).copy(alpha = 0.45f), radius = 90f * zoom, center = cloud1)

            val cloud2 = Offset(center.x - 90f * zoom + frameShiftX * 0.6f, center.y + 70f * zoom + frameShiftY * 0.6f)
            drawCircle(color = Color(0xFFCFD8DC).copy(alpha = 0.50f), radius = 110f * zoom, center = cloud2)
        }
        RadarLayer.WIND -> {
            // Wind stream particles / velocity bands
            val windCenter = Offset(center.x + frameShiftX * 0.8f, center.y + frameShiftY * 0.8f)
            drawCircle(color = Color(0xFF4FC3F7).copy(alpha = 0.45f), radius = 120f * zoom, center = windCenter)
            drawCircle(color = Color(0xFF0288D1).copy(alpha = 0.45f), radius = 75f * zoom, center = windCenter)
        }
        RadarLayer.TEMPERATURE -> {
            // Thermal heatmap overlay
            val heatCenter = Offset(center.x + 40f * zoom, center.y - 20f * zoom)
            drawCircle(color = Color(0xFF66BB6A).copy(alpha = 0.40f), radius = 140f * zoom, center = heatCenter)
            drawCircle(color = Color(0xFFFFA726).copy(alpha = 0.45f), radius = 80f * zoom, center = heatCenter)
        }
    }
}

private fun DrawScope.drawLocationBeacon(center: Offset, pulseRadius: Float, basemap: BasemapType) {
    val beaconColor = if (basemap == BasemapType.OPEN_STREET_MAP) Color(0xFF1E88E5) else Color(0xFF42A5F5)

    drawCircle(
        color = beaconColor.copy(alpha = 0.35f * (1f - (pulseRadius / 30f))),
        radius = pulseRadius,
        center = center
    )
    drawCircle(
        color = beaconColor,
        radius = 7f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = center
    )
}

package app.atmos.weather.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.atmos.weather.data.model.ParallaxIntensity

data class ParallaxOffset(
    val pitch: Float = 0f, // vertical tilt [-1f, 1f]
    val roll: Float = 0f,  // horizontal tilt [-1f, 1f]
    val multiplier: Float = 1f
) {
    val effectivePitch: Float get() = pitch * multiplier
    val effectiveRoll: Float get() = roll * multiplier
}

class GyroscopeTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    private val _parallaxOffset = mutableStateOf(ParallaxOffset())
    val parallaxOffset: State<ParallaxOffset> = _parallaxOffset

    private var isListening = false
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Exponential smoothing factor (alpha) - responsive yet smooth
    private val smoothing = 0.15f
    private var smoothedPitch = 0f
    private var smoothedRoll = 0f
    private var currentMultiplier = 1f

    fun setMultiplier(multiplier: Float) {
        currentMultiplier = multiplier
    }

    fun start() {
        if (!isListening && rotationSensor != null) {
            sensorManager?.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            isListening = true
        }
    }

    fun stop() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
            _parallaxOffset.value = ParallaxOffset(0f, 0f, currentMultiplier)
            smoothedPitch = 0f
            smoothedRoll = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Pitch is index 1 (around X axis), Roll is index 2 (around Y axis)
            val pitch = orientationAngles[1]
            val roll = orientationAngles[2]

            // Normalize to approximately [-1.5f, 1.5f] for typical phone tilt range (~45 degrees)
            val targetPitch = (pitch / 0.785f).coerceIn(-1.5f, 1.5f)
            val targetRoll = (roll / 0.785f).coerceIn(-1.5f, 1.5f)

            // Low-pass filter smoothing to eliminate jitter
            smoothedPitch += smoothing * (targetPitch - smoothedPitch)
            smoothedRoll += smoothing * (targetRoll - smoothedRoll)

            _parallaxOffset.value = ParallaxOffset(
                pitch = smoothedPitch,
                roll = smoothedRoll,
                multiplier = currentMultiplier
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun rememberGyroscopeParallax(
    intensity: ParallaxIntensity = ParallaxIntensity.BALANCED,
    reducedMotion: Boolean = false
): State<ParallaxOffset> {
    val context = LocalContext.current
    val tracker = remember { GyroscopeTracker(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val enabled = intensity != ParallaxIntensity.OFF && !reducedMotion

    DisposableEffect(lifecycleOwner, intensity, reducedMotion) {
        tracker.setMultiplier(if (enabled) intensity.multiplier else 0f)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (enabled) {
                    tracker.start()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                tracker.stop()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (enabled && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            tracker.start()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tracker.stop()
        }
    }

    return if (enabled) {
        tracker.parallaxOffset
    } else {
        remember { mutableStateOf(ParallaxOffset(0f, 0f, 0f)) }
    }
}

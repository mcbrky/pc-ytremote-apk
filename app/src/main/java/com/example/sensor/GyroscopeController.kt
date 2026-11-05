package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow

/**
 * High-Precision, Ultra-Smooth Gyroscopic Air Mouse Controller.
 * Features:
 * - Continuous soft-knee deadband (zero micro-jump when hand starts or stops moving)
 * - Dual-zone adaptive low-pass filter (heavy smoothing for slow aiming, responsive tracking for fast sweeps)
 * - Stationary bias cancellation (auto-zero drift compensator)
 * - Pause/Resume support (halts mouse movement while typing or keyboard is open)
 */
class GyroscopeController(
    private val context: Context,
    private val onMouseMotion: (dx: Float, dy: Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    // Primary: Gyroscope sensor measuring true angular velocity (rad/s)
    private val gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    // Fallback: Rotation vector if device lacks a physical gyroscope
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var activeSensor: Sensor? = null
    private var isEnabled = false
    var isPaused: Boolean = false

    private var sensitivity: Float = 2.0f
    private var smoothing: Float = 0.85f // 0.50f (very snappy) .. 0.95f (ultra smooth)

    private var lastTimestamp: Long = 0L
    private var smoothedDx = 0f
    private var smoothedDy = 0f

    // Soft-knee deadzone in rad/s to filter resting hand tremors and table vibrations
    private val gyroDeadzone = 0.055f

    // Stationary auto-bias drift compensators
    private var biasYaw = 0f
    private var biasPitch = 0f
    private var stationarySamples = 0

    fun start(sensitivity: Float, smoothing: Float = 0.85f) {
        this.sensitivity = sensitivity.coerceIn(0.5f, 4.0f)
        this.smoothing = smoothing.coerceIn(0.50f, 0.95f)
        if (isEnabled || sensorManager == null) return

        lastTimestamp = 0L
        smoothedDx = 0f
        smoothedDy = 0f
        biasYaw = 0f
        biasPitch = 0f
        stationarySamples = 0
        isPaused = false

        val targetSensor = gyroSensor ?: rotationSensor
        if (targetSensor == null) return

        val registered = sensorManager.registerListener(
            this,
            targetSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        if (registered) {
            activeSensor = targetSensor
            isEnabled = true
        }
    }

    fun stop() {
        if (!isEnabled) return
        isEnabled = false
        isPaused = false
        lastTimestamp = 0L
        smoothedDx = 0f
        smoothedDy = 0f
        activeSensor = null
        sensorManager?.unregisterListener(this)
    }

    fun pause() {
        isPaused = true
        smoothedDx = 0f
        smoothedDy = 0f
    }

    fun resume() {
        isPaused = false
        lastTimestamp = 0L
        smoothedDx = 0f
        smoothedDy = 0f
    }

    fun updateSensitivity(sensitivity: Float) {
        this.sensitivity = sensitivity.coerceIn(0.5f, 4.0f)
    }

    fun updateSmoothing(smoothing: Float) {
        this.smoothing = smoothing.coerceIn(0.50f, 0.95f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || isPaused || event == null) return

        val currentTimestamp = event.timestamp
        if (lastTimestamp == 0L) {
            lastTimestamp = currentTimestamp
            return
        }

        val dt = ((currentTimestamp - lastTimestamp) * 1.0e-9f).coerceIn(0.005f, 0.05f)
        lastTimestamp = currentTimestamp

        var rawDx = 0f
        var rawDy = 0f

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val wx = event.values[0]
            val wy = event.values[1]
            val wz = event.values[2]

            // In portrait handheld orientation:
            // Tilting phone up/down = Pitch (wx)
            // Turning phone left/right = combination of Yaw (wz) and Roll (wy)
            var angularYaw = -wz * 0.75f - wy * 0.40f
            var angularPitch = -wx

            val rawSpeed = hypot(angularYaw, angularPitch)

            if (rawSpeed < gyroDeadzone) {
                // Device is stationary / hand tremor: update auto-zero bias compensator
                stationarySamples++
                val biasWeight = 0.05f
                biasYaw = biasYaw * (1f - biasWeight) + angularYaw * biasWeight
                biasPitch = biasPitch * (1f - biasWeight) + angularPitch * biasWeight
                rawDx = 0f
                rawDy = 0f
            } else {
                // Device is moving: subtract stationary bias to eliminate drift
                angularYaw -= biasYaw
                angularPitch -= biasPitch

                val speed = hypot(angularYaw, angularPitch)
                if (speed > gyroDeadzone) {
                    // Continuous soft-knee curve: excess speed starts smoothly from 0!
                    val excessSpeed = speed - gyroDeadzone
                    val scaleFactor = (excessSpeed / speed).pow(1.15f)
                    val effectiveYaw = angularYaw * scaleFactor
                    val effectivePitch = angularPitch * scaleFactor

                    // Dynamic dual-zone response:
                    // Precision aiming: ultra gentle scaling
                    // Fast sweeps: responsive acceleration
                    val accel = when {
                        excessSpeed < 0.25f -> 0.65f + (excessSpeed / 0.25f) * 0.35f
                        excessSpeed > 1.0f -> 1.25f
                        else -> 1.0f + (excessSpeed - 0.25f) * 0.33f
                    }

                    val baseScale = 24f * sensitivity
                    rawDx = effectiveYaw * baseScale * accel
                    rawDy = effectivePitch * baseScale * accel
                }
            }
        } else {
            // Fallback for devices without hardware gyroscope
            val roll = event.values.getOrNull(2) ?: 0f
            val pitch = event.values.getOrNull(1) ?: 0f
            if (abs(roll) > 0.06f) rawDx = (roll - 0.06f) * 9f * sensitivity
            if (abs(pitch) > 0.06f) rawDy = (pitch - 0.06f) * 9f * sensitivity
        }

        // Clamp maximum displacement per event so cursor never teleports wildly
        val maxStep = 22f * (sensitivity / 1.5f).coerceAtLeast(0.7f)
        rawDx = rawDx.coerceIn(-maxStep, maxStep)
        rawDy = rawDy.coerceIn(-maxStep, maxStep)

        // Adaptive Exponential Moving Average (EMA) smoothing:
        // When moving slowly, apply strong smoothing factor for rock-solid precision.
        // When flicking or sweeping, reduce smoothing for instant responsiveness.
        val targetAlpha = 1.0f - smoothing.coerceIn(0.50f, 0.95f)
        val movementMagnitude = hypot(rawDx, rawDy)
        val adaptiveAlpha = if (movementMagnitude > 8f) {
            (targetAlpha * 1.8f).coerceAtMost(0.45f)
        } else {
            (targetAlpha * 0.85f).coerceAtLeast(0.08f)
        }

        smoothedDx = smoothedDx * (1f - adaptiveAlpha) + rawDx * adaptiveAlpha
        smoothedDy = smoothedDy * (1f - adaptiveAlpha) + rawDy * adaptiveAlpha

        // Only emit if moving beyond threshold to avoid lingering micro-delta
        if (abs(smoothedDx) > 0.28f || abs(smoothedDy) > 0.28f) {
            onMouseMotion(smoothedDx, smoothedDy)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}

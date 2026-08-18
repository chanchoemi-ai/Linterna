package com.example.flashlight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FlashlightMode {
    NORMAL,
    STROBE,
    SOS,
    SCREEN
}

data class FlashlightState(
    val isTorchOn: Boolean = false,
    val isPhysicalFlashAvailable: Boolean = true,
    val mode: FlashlightMode = FlashlightMode.NORMAL,
    val strobeFrequencyHz: Float = 5f,
    val batteryPercentage: Int = 100,
    val isBatteryCharging: Boolean = false,
    val timerRemainingSeconds: Int = 0,
    val timerSelectedMinutes: Int = 0,
    val screenLightBrightness: Float = 1f,
    val screenLightColorIndex: Int = 0,
    val isScreenLightActive: Boolean = false
)

class FlashlightManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "FlashlightManager"
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var rearCameraId: String? = null

    private val _state = MutableStateFlow(FlashlightState())
    val state: StateFlow<FlashlightState> = _state.asStateFlow()

    private var strobeJob: Job? = null
    private var sosJob: Job? = null
    private var timerJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == rearCameraId) {
                if (_state.value.mode == FlashlightMode.NORMAL) {
                    _state.value = _state.value.copy(isTorchOn = enabled)
                }
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == rearCameraId) {
                Log.w(tag, "Torch mode unavailable for camera: $cameraId")
            }
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val pct = if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100).toInt()
                } else {
                    100
                }

                _state.value = _state.value.copy(
                    batteryPercentage = pct,
                    isBatteryCharging = isCharging
                )
            }
        }
    }

    init {
        detectFlashlight()
        registerTorchCallback()
        registerBatteryReceiver()
    }

    private fun detectFlashlight() {
        var hasFlash = false
        cameraManager?.let { manager ->
            try {
                for (id in manager.cameraIdList) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (flashAvailable && (lensFacing == CameraCharacteristics.LENS_FACING_BACK || rearCameraId == null)) {
                        rearCameraId = id
                        hasFlash = true
                        if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error inspecting camera characteristics", e)
            }
        }
        _state.value = _state.value.copy(isPhysicalFlashAvailable = hasFlash && rearCameraId != null)
    }

    private fun registerTorchCallback() {
        try {
            cameraManager?.registerTorchCallback(torchCallback, null)
        } catch (e: Exception) {
            Log.e(tag, "Error registering torch callback", e)
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            Log.e(tag, "Error registering battery receiver", e)
        }
    }

    fun triggerHaptic(strong: Boolean = false) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (strong) {
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(25, 140)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (strong) 45 else 25)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails or not allowed
        }
    }

    fun toggleTorch() {
        triggerHaptic(strong = true)
        when (_state.value.mode) {
            FlashlightMode.NORMAL -> {
                val nextState = !_state.value.isTorchOn
                setHardwareTorch(nextState)
                _state.value = _state.value.copy(isTorchOn = nextState)
                if (nextState && _state.value.timerSelectedMinutes > 0) {
                    startTimer(_state.value.timerSelectedMinutes)
                } else if (!nextState) {
                    stopTimer()
                }
            }
            FlashlightMode.STROBE -> {
                if (strobeJob?.isActive == true) {
                    stopStrobe()
                } else {
                    startStrobe(_state.value.strobeFrequencyHz)
                }
            }
            FlashlightMode.SOS -> {
                if (sosJob?.isActive == true) {
                    stopSos()
                } else {
                    startSos()
                }
            }
            FlashlightMode.SCREEN -> {
                val next = !_state.value.isScreenLightActive
                _state.value = _state.value.copy(
                    isScreenLightActive = next,
                    isTorchOn = next
                )
            }
        }
    }

    fun setHardwareTorch(enabled: Boolean) {
        val camId = rearCameraId
        if (camId != null && cameraManager != null) {
            try {
                cameraManager.setTorchMode(camId, enabled)
            } catch (e: CameraAccessException) {
                Log.e(tag, "Failed to set torch mode: $enabled", e)
            } catch (e: Exception) {
                Log.e(tag, "Unexpected error setting torch mode", e)
            }
        }
    }

    fun setMode(mode: FlashlightMode) {
        if (_state.value.mode == mode) return
        triggerHaptic(strong = false)

        // Stop current active modes
        stopStrobe()
        stopSos()
        setHardwareTorch(false)

        _state.value = _state.value.copy(
            mode = mode,
            isTorchOn = false,
            isScreenLightActive = false
        )

        // If switching to SCREEN mode, we can auto-enable screen light
        if (mode == FlashlightMode.SCREEN) {
            _state.value = _state.value.copy(
                isScreenLightActive = true,
                isTorchOn = true
            )
        }
    }

    fun setStrobeFrequency(frequencyHz: Float) {
        _state.value = _state.value.copy(strobeFrequencyHz = frequencyHz)
        if (_state.value.mode == FlashlightMode.STROBE && strobeJob?.isActive == true) {
            startStrobe(frequencyHz)
        }
    }

    private fun startStrobe(freqHz: Float) {
        stopStrobe()
        stopSos()
        _state.value = _state.value.copy(isTorchOn = true)
        val periodMs = (1000f / freqHz.coerceIn(1f, 25f)).toLong()
        val halfPeriod = (periodMs / 2).coerceAtLeast(20L)

        strobeJob = scope.launch(Dispatchers.Default) {
            var lightOn = false
            while (isActive) {
                lightOn = !lightOn
                setHardwareTorch(lightOn)
                _state.value = _state.value.copy(isTorchOn = lightOn)
                delay(halfPeriod)
            }
        }
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        setHardwareTorch(false)
        if (_state.value.mode == FlashlightMode.STROBE) {
            _state.value = _state.value.copy(isTorchOn = false)
        }
    }

    private fun startSos() {
        stopSos()
        stopStrobe()
        _state.value = _state.value.copy(isTorchOn = true)

        // SOS pattern: 3 short (200ms), 3 long (600ms), 3 short (200ms)
        // pause between flashes: 200ms, pause between letters: 600ms, pause between word: 1400ms
        sosJob = scope.launch(Dispatchers.Default) {
            val dot = 180L
            val dash = 540L
            val symbolGap = 180L
            val letterGap = 500L
            val wordGap = 1200L

            while (isActive) {
                // S (...)
                for (i in 0 until 3) {
                    if (!isActive) break
                    flashPulse(dot, symbolGap)
                }
                delay(letterGap)

                // O (---)
                for (i in 0 until 3) {
                    if (!isActive) break
                    flashPulse(dash, symbolGap)
                }
                delay(letterGap)

                // S (...)
                for (i in 0 until 3) {
                    if (!isActive) break
                    flashPulse(dot, symbolGap)
                }
                delay(wordGap)
            }
        }
    }

    private suspend fun flashPulse(onDuration: Long, offDuration: Long) {
        setHardwareTorch(true)
        _state.value = _state.value.copy(isTorchOn = true)
        delay(onDuration)
        setHardwareTorch(false)
        _state.value = _state.value.copy(isTorchOn = false)
        delay(offDuration)
    }

    private fun stopSos() {
        sosJob?.cancel()
        sosJob = null
        setHardwareTorch(false)
        if (_state.value.mode == FlashlightMode.SOS) {
            _state.value = _state.value.copy(isTorchOn = false)
        }
    }

    fun selectTimerMinutes(minutes: Int) {
        triggerHaptic()
        _state.value = _state.value.copy(timerSelectedMinutes = minutes)
        if (minutes > 0 && _state.value.isTorchOn) {
            startTimer(minutes)
        } else if (minutes == 0) {
            stopTimer()
        }
    }

    private fun startTimer(minutes: Int) {
        stopTimer()
        val totalSeconds = minutes * 60
        _state.value = _state.value.copy(timerRemainingSeconds = totalSeconds)

        timerJob = scope.launch(Dispatchers.Default) {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(timerRemainingSeconds = remaining)
            }
            if (remaining <= 0 && isActive) {
                // Turn off torch and reset timer
                turnOffAll()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(timerRemainingSeconds = 0)
    }

    fun setScreenLightColor(index: Int) {
        _state.value = _state.value.copy(screenLightColorIndex = index)
    }

    fun setScreenLightBrightness(brightness: Float) {
        _state.value = _state.value.copy(screenLightBrightness = brightness)
    }

    fun turnOffAll() {
        stopStrobe()
        stopSos()
        stopTimer()
        setHardwareTorch(false)
        _state.value = _state.value.copy(
            isTorchOn = false,
            isScreenLightActive = false
        )
    }

    fun onDestroy() {
        turnOffAll()
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            // Ignore
        }
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }
}

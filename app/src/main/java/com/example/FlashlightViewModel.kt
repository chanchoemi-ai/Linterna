package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashlight.FlashlightManager
import com.example.flashlight.FlashlightMode
import com.example.flashlight.FlashlightState
import kotlinx.coroutines.flow.StateFlow

class FlashlightViewModel(application: Application) : AndroidViewModel(application) {

    private val flashlightManager = FlashlightManager(application.applicationContext, viewModelScope)
    val state: StateFlow<FlashlightState> = flashlightManager.state

    fun toggleTorch() {
        flashlightManager.toggleTorch()
    }

    fun setMode(mode: FlashlightMode) {
        flashlightManager.setMode(mode)
    }

    fun setStrobeFrequency(frequencyHz: Float) {
        flashlightManager.setStrobeFrequency(frequencyHz)
    }

    fun selectTimerMinutes(minutes: Int) {
        flashlightManager.selectTimerMinutes(minutes)
    }

    fun setScreenLightColor(index: Int) {
        flashlightManager.setScreenLightColor(index)
    }

    fun setScreenLightBrightness(brightness: Float) {
        flashlightManager.setScreenLightBrightness(brightness)
    }

    fun closeFullScreen() {
        flashlightManager.setMode(FlashlightMode.NORMAL)
    }

    override fun onCleared() {
        super.onCleared()
        flashlightManager.onDestroy()
    }
}

package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FlashlightScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FlashlightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()

                // Keep screen active while flashlight or screen light is on
                LaunchedEffect(state.isTorchOn) {
                    if (state.isTorchOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                FlashlightScreen(
                    state = state,
                    onToggle = viewModel::toggleTorch,
                    onModeSelect = viewModel::setMode,
                    onStrobeFrequencyChange = viewModel::setStrobeFrequency,
                    onTimerSelect = viewModel::selectTimerMinutes,
                    onScreenColorSelect = viewModel::setScreenLightColor,
                    onScreenBrightnessChange = viewModel::setScreenLightBrightness,
                    onCloseFullScreen = viewModel::closeFullScreen
                )
            }
        }
    }
}


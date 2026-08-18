package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashlight.FlashlightMode
import com.example.flashlight.FlashlightState
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.GlassFrostBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

val ScreenColors = listOf(
    Color(0xFFFFFFFF) to "Blanco Puro",
    Color(0xFFFFF7ED) to "Luz Cálida",
    Color(0xFFE0F2FE) to "Azul Neón",
    Color(0xFFFEE2E2) to "Rojo Noche",
    Color(0xFFFEF08A) to "Ámbar Solar"
)

@Composable
fun FlashlightScreen(
    state: FlashlightState,
    onToggle: () -> Unit,
    onModeSelect: (FlashlightMode) -> Unit,
    onStrobeFrequencyChange: (Float) -> Unit,
    onTimerSelect: (Int) -> Unit,
    onScreenColorSelect: (Int) -> Unit,
    onScreenBrightnessChange: (Float) -> Unit,
    onCloseFullScreen: () -> Unit
) {
    var showTimerDialog by remember { mutableStateOf(false) }

    // If fullscreen screen light mode is active
    if (state.mode == FlashlightMode.SCREEN && state.isScreenLightActive) {
        FullScreenLightView(
            color = ScreenColors.getOrElse(state.screenLightColorIndex) { ScreenColors[0] }.first,
            brightness = state.screenLightBrightness,
            onClose = onCloseFullScreen
        )
        return
    }

    LiquidBackground(
        isLit = state.isTorchOn,
        modifier = Modifier.fillMaxSize()
    ) {
        LiquidRippleRings(isLit = state.isTorchOn)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // IMMERSIVE HEADER
            ImmersiveHeader(
                state = state,
                onTimerClick = { showTimerDialog = !showTimerDialog }
            )

            // CENTER STAGE: Hero Circular Glass Button & Status Meter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                LiquidFlashlightButton(
                    isLit = state.isTorchOn,
                    onToggle = onToggle
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Status Pill Meter (from Immersive UI template)
                StatusPillMeter(
                    isLit = state.isTorchOn,
                    mode = state.mode,
                    strobeFreq = state.strobeFrequencyHz
                )
            }

            // IMMERSIVE FOOTER: Thin Divider & Nav Bar
            ImmersiveFooter(
                state = state,
                onModeSelect = onModeSelect,
                onStrobeFreqChange = onStrobeFrequencyChange,
                onScreenColorSelect = onScreenColorSelect
            )
        }

        // Timer Selection Overlay Card
        AnimatedVisibility(
            visible = showTimerDialog,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
        ) {
            TimerSelectionCard(
                selectedMinutes = state.timerSelectedMinutes,
                onSelectMinutes = { minutes ->
                    onTimerSelect(minutes)
                    showTimerDialog = false
                },
                onDismiss = { showTimerDialog = false }
            )
        }
    }
}

/**
 * Immersive Header with System Status & Glass Badges.
 */
@Composable
private fun ImmersiveHeader(
    state: FlashlightState,
    onTimerClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // System Status (Left)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "SYSTEM",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = Slate500
            )
            val systemStatusText = when {
                state.isTorchOn && state.mode == FlashlightMode.NORMAL -> "Active"
                state.isTorchOn && state.mode == FlashlightMode.SOS -> "SOS Beacon"
                state.isTorchOn && state.mode == FlashlightMode.STROBE -> "Strobe ${state.strobeFrequencyHz.toInt()}Hz"
                state.isTorchOn && state.mode == FlashlightMode.SCREEN -> "Screen Light"
                else -> "Ready"
            }
            Text(
                text = systemStatusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (state.isTorchOn) Amber400 else Slate200
            )
        }

        // Actions & Badges (Right)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val batteryIcon = when {
                    state.isBatteryCharging -> Icons.Default.BatteryChargingFull
                    state.batteryPercentage <= 15 -> Icons.Default.BatteryAlert
                    else -> Icons.Default.BatteryFull
                }
                val iconTint = when {
                    state.isBatteryCharging -> NeonCyan
                    state.batteryPercentage <= 15 -> Color(0xFFEF4444)
                    else -> Slate400
                }

                Icon(
                    imageVector = batteryIcon,
                    contentDescription = "Batería",
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "${state.batteryPercentage}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate200
                )
            }

            // Timer Button Glass Capsule (w-10 h-10 rounded-2xl bg-white/5 border border-white/10)
            val timerActive = state.timerRemainingSeconds > 0
            val timerText = if (timerActive) {
                val m = state.timerRemainingSeconds / 60
                val s = state.timerRemainingSeconds % 60
                String.format(Locale.getDefault(), "%d:%02d", m, s)
            } else null

            Box(
                modifier = Modifier
                    .size(if (timerActive) 68.dp else 40.dp, 40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (timerActive) Amber500.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f))
                    .border(
                        1.dp,
                        if (timerActive) Amber400.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onTimerClick)
                    .testTag("timer_badge_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Temporizador",
                        tint = if (timerActive) Amber400 else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                    if (timerText != null) {
                        Text(
                            text = timerText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber400
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal Status Pill Meter below Hero Button.
 */
@Composable
private fun StatusPillMeter(
    isLit: Boolean,
    mode: FlashlightMode,
    strobeFreq: Float
) {
    val meterFill by animateFloatAsState(
        targetValue = if (isLit) {
            when (mode) {
                FlashlightMode.NORMAL -> 1.0f
                FlashlightMode.STROBE -> (strobeFreq / 20f).coerceIn(0.1f, 1f)
                FlashlightMode.SOS -> 0.75f
                FlashlightMode.SCREEN -> 0.90f
            }
        } else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "meter_fill"
    )

    val meterLabel = when {
        !isLit -> "STANDBY"
        mode == FlashlightMode.NORMAL -> "TORCH 100%"
        mode == FlashlightMode.STROBE -> "STROBE ${strobeFreq.toInt()} HZ"
        mode == FlashlightMode.SOS -> "SOS BEACON"
        mode == FlashlightMode.SCREEN -> "SCREEN LIGHT"
        else -> "STANDBY"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Minimal bar indicator
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(meterFill)
                    .clip(CircleShape)
                    .background(Amber400)
            )
        }

        // Tracking status text
        Text(
            text = meterLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (isLit) Amber400.copy(alpha = 0.9f) else Slate500,
            modifier = Modifier.testTag("flashlight_state_text")
        )
    }
}

/**
 * Immersive Footer with Gradient Divider and Navigation Bar.
 */
@Composable
private fun ImmersiveFooter(
    state: FlashlightState,
    onModeSelect: (FlashlightMode) -> Unit,
    onStrobeFreqChange: (Float) -> Unit,
    onScreenColorSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Additional Controls for Strobe Mode
        AnimatedVisibility(
            visible = state.mode == FlashlightMode.STROBE,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        isLit = state.isTorchOn,
                        alpha = 0.08f,
                        elevation = 6.dp
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FRECUENCIA",
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.strobeFrequencyHz.toInt()} Hz",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber400
                    )
                }

                Slider(
                    value = state.strobeFrequencyHz,
                    onValueChange = onStrobeFreqChange,
                    valueRange = 1f..20f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = Amber400,
                        activeTrackColor = Amber400,
                        inactiveTrackColor = Color(0x26FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("strobe_slider")
                )
            }
        }

        // Additional Controls for Screen Light Mode
        AnimatedVisibility(
            visible = state.mode == FlashlightMode.SCREEN,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        isLit = state.isTorchOn,
                        alpha = 0.08f,
                        elevation = 6.dp
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "COLOR DE PANTALLA",
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScreenColors.forEachIndexed { index, (color, _) ->
                        val isSelected = state.screenLightColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Amber400 else Color(0x33000000),
                                    shape = CircleShape
                                )
                                .clickable { onScreenColorSelect(index) }
                                .testTag("color_preset_$index")
                        )
                    }
                }
            }
        }

        // Thin Gradient Separator line: w-full h-[1px] bg-gradient-to-r from-transparent via-white/10 to-transparent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Bar Modes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImmersiveNavModeItem(
                text = "Torch",
                icon = Icons.Default.FlashOn,
                isSelected = state.mode == FlashlightMode.NORMAL,
                onClick = { onModeSelect(FlashlightMode.NORMAL) },
                testTag = "mode_normal"
            )
            ImmersiveNavModeItem(
                text = "SOS",
                icon = Icons.Default.Warning,
                isSelected = state.mode == FlashlightMode.SOS,
                onClick = { onModeSelect(FlashlightMode.SOS) },
                testTag = "mode_sos"
            )
            ImmersiveNavModeItem(
                text = "Strobe",
                icon = Icons.Default.Bolt,
                isSelected = state.mode == FlashlightMode.STROBE,
                onClick = { onModeSelect(FlashlightMode.STROBE) },
                testTag = "mode_strobe"
            )
            ImmersiveNavModeItem(
                text = "Screen",
                icon = Icons.Default.Smartphone,
                isSelected = state.mode == FlashlightMode.SCREEN,
                onClick = { onModeSelect(FlashlightMode.SCREEN) },
                testTag = "mode_screen"
            )
        }
    }
}

/**
 * Minimal Timer Selection Dialog Card.
 */
@Composable
private fun TimerSelectionCard(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "Desactivado",
        1 to "1 min",
        3 to "3 min",
        5 to "5 min",
        10 to "10 min",
        15 to "15 min"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                isLit = selectedMinutes > 0,
                alpha = 0.18f,
                elevation = 16.dp
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TEMPORIZADOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Slate200
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Slate400
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.take(3).forEach { (mins, label) ->
                val isSelected = selectedMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Amber500.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
                        .border(
                            1.dp,
                            if (isSelected) Amber400 else GlassFrostBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMinutes(mins) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Amber400 else Slate200
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.drop(3).forEach { (mins, label) ->
                val isSelected = selectedMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Amber500.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
                        .border(
                            1.dp,
                            if (isSelected) Amber400 else GlassFrostBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMinutes(mins) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Amber400 else Slate200
                    )
                }
            }
        }
    }
}

/**
 * Fullscreen Screen Light View with tap to close
 */
@Composable
private fun FullScreenLightView(
    color: Color,
    brightness: Float,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .clickable(onClick = onClose)
            .testTag("fullscreen_screen_light")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Toca la pantalla para salir",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

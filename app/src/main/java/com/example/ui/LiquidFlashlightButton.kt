package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.GlassFrostBorder
import com.example.ui.theme.GlassFrostBorderLit
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.TorchBeamColor
import kotlin.math.sin

/**
 * Immersive UI Hero Flashlight Button.
 * Circular glass sphere (w-56 h-56) with outer aura blur, inner bevel ring,
 * liquid glass torch with realistic fluid, optical lens flare, and animated power switch.
 */
@Composable
fun LiquidFlashlightButton(
    isLit: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press scaling physics
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 450f),
        label = "press_scale"
    )

    // Lit state animated transition factor (0f = off, 1f = on)
    val litProgress by animateFloatAsState(
        targetValue = if (isLit) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 300f),
        label = "lit_progress"
    )

    // Switch slider displacement (0f = bottom/off, 1f = top/on)
    val switchProgress by animateFloatAsState(
        targetValue = if (isLit) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 350f),
        label = "switch_progress"
    )

    // Continuous pulse transition
    val infiniteTransition = rememberInfiniteTransition(label = "immersive_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val liquidPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_phase"
    )

    Box(
        modifier = modifier
            .size(260.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient blur aura (-inset-4 bg-white/5 rounded-full blur-2xl)
        Box(
            modifier = Modifier
                .size(250.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isLit) Amber400.copy(alpha = 0.25f * pulseGlow) else Color.White.copy(alpha = 0.05f)),
                                (if (isLit) Amber500.copy(alpha = 0.10f) else Color.Transparent),
                                Color.Transparent
                            ),
                            radius = size.width / 2f
                        )
                    )
                }
        )

        // Main Circular Frosted Glass Button (w-56 h-56 / 224dp)
        Box(
            modifier = Modifier
                .size(224.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = if (isLit) Amber400.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.8f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isLit) 0.20f else 0.12f),
                            Color(0xFF1E293B).copy(alpha = if (isLit) 0.25f else 0.18f),
                            Color(0xFF0F172A).copy(alpha = if (isLit) 0.40f else 0.30f),
                            Color.White.copy(alpha = if (isLit) 0.10f else 0.04f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = if (isLit) listOf(
                            Amber400.copy(alpha = 0.8f),
                            Color.White.copy(alpha = 0.3f),
                            Amber500.copy(alpha = 0.4f)
                        ) else listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle
                )
                .testTag("flashlight_torch_button"),
            contentAlignment = Alignment.Center
        ) {
            // Inner ring bezel (inset-2 rounded-full border border-white/5)
            Box(
                modifier = Modifier
                    .size(208.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(300f, 300f)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.06f),
                        shape = CircleShape
                    )
            )

            // Content inside the button: Canvas drawing the Torch + Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                content = {
                    Box(
                        modifier = Modifier.size(width = 160.dp, height = 150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Volumetric Beam if lit
                            if (litProgress > 0.01f) {
                                drawVolumetricBeam(
                                    litProgress = litProgress,
                                    pulse = pulseGlow,
                                    phase = liquidPhase
                                )
                            }

                            // Flashlight Graphic
                            drawFlashlightTorch(
                                litProgress = litProgress,
                                switchProgress = switchProgress,
                                liquidPhase = liquidPhase
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Minimal uppercase tracking power label
                    Text(
                        text = if (isLit) "POWER ON" else "POWER OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = if (isLit) Amber300.copy(alpha = 0.95f) else Slate500
                    )
                }
            )
        }
    }
}

/**
 * Draws the volumetric light beam radiating from the torch lens.
 */
private fun DrawScope.drawVolumetricBeam(
    litProgress: Float,
    pulse: Float,
    phase: Float
) {
    val torchLensCenterX = size.width / 2f
    val torchLensY = size.height * 0.28f

    val beamTopWidth = size.width * (0.80f * pulse) * litProgress
    val beamBottomWidth = size.width * 0.22f

    val path = Path().apply {
        moveTo(torchLensCenterX - beamBottomWidth / 2f, torchLensY)
        lineTo(torchLensCenterX - beamTopWidth / 2f, 0f)
        lineTo(torchLensCenterX + beamTopWidth / 2f, 0f)
        lineTo(torchLensCenterX + beamBottomWidth / 2f, torchLensY)
        close()
    }

    val beamBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            TorchBeamColor.copy(alpha = 0.08f * litProgress * pulse),
            Amber400.copy(alpha = 0.25f * litProgress * pulse),
            Color.White.copy(alpha = 0.60f * litProgress)
        ),
        startY = 0f,
        endY = torchLensY
    )

    drawPath(path = path, brush = beamBrush)

    // Shimmering dust particles in the beam
    val pCount = 5
    for (i in 0 until pCount) {
        val pProgress = ((phase + i * (Math.PI.toFloat() * 2 / pCount)) % (Math.PI.toFloat() * 2)) / (Math.PI.toFloat() * 2)
        val py = torchLensY * (1f - pProgress)
        val pSpread = (1f - py / torchLensY) * (beamTopWidth / 2.2f)
        val px = torchLensCenterX + sin((phase * 2 + i).toDouble()).toFloat() * pSpread
        val pAlpha = sin((pProgress * Math.PI).toDouble()).toFloat() * 0.5f * litProgress

        drawCircle(
            color = Color.White.copy(alpha = pAlpha.coerceIn(0f, 1f)),
            radius = 2.dp.toPx() * (1f - pProgress * 0.5f),
            center = Offset(px, py)
        )
    }
}

/**
 * Draws the stylized liquid glass flashlight torch body, head, lens, and switch.
 */
private fun DrawScope.drawFlashlightTorch(
    litProgress: Float,
    switchProgress: Float,
    liquidPhase: Float
) {
    val centerX = size.width / 2f
    val baseY = size.height * 0.28f

    // Dimensions calibrated for the circular button frame
    val headTopWidth = size.width * 0.32f
    val headBottomWidth = size.width * 0.22f
    val headHeight = size.height * 0.13f

    val bodyWidth = size.width * 0.18f
    val bodyHeight = size.height * 0.44f
    val bodyRadius = 10.dp.toPx()

    val tailCapHeight = size.height * 0.05f
    val tailCapWidth = size.width * 0.19f

    // 1. AURA GLOW BEHIND THE HEAD
    val glowRadius = size.width * (0.35f + 0.15f * litProgress)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (litProgress > 0.5f) Amber400 else NeonCyan).copy(alpha = 0.35f * litProgress + 0.05f),
                (if (litProgress > 0.5f) Amber500 else Color(0xFF0284C7)).copy(alpha = 0.12f * litProgress + 0.02f),
                Color.Transparent
            ),
            center = Offset(centerX, baseY + headHeight * 0.3f),
            radius = glowRadius
        ),
        center = Offset(centerX, baseY + headHeight * 0.3f),
        radius = glowRadius
    )

    // 2. TORCH BODY (CYLINDRICAL LIQUID GLASS HANDLE)
    val bodyTopY = baseY + headHeight
    val bodyRect = Rect(
        offset = Offset(centerX - bodyWidth / 2f, bodyTopY),
        size = Size(bodyWidth, bodyHeight)
    )

    // Glass body background gradient
    val bodyBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.14f + 0.08f * litProgress),
            Color(0xFF1E293B).copy(alpha = 0.50f),
            Color(0xFF0F172A).copy(alpha = 0.75f),
            Color.White.copy(alpha = 0.20f + 0.10f * litProgress)
        ),
        startX = bodyRect.left,
        endX = bodyRect.right
    )

    drawRoundRect(
        brush = bodyBrush,
        topLeft = bodyRect.topLeft,
        size = bodyRect.size,
        cornerRadius = CornerRadius(bodyRadius, bodyRadius)
    )

    // Liquid Chamber with fluid
    val liquidPadding = 3.dp.toPx()
    val liquidInnerRect = Rect(
        offset = Offset(bodyRect.left + liquidPadding, bodyRect.top + liquidPadding),
        size = Size(bodyWidth - liquidPadding * 2, bodyHeight - liquidPadding * 2)
    )

    val liquidFillPercent = 0.25f + 0.70f * litProgress
    val liquidHeight = liquidInnerRect.height * liquidFillPercent
    val liquidTopY = liquidInnerRect.bottom - liquidHeight

    val liquidBrush = Brush.verticalGradient(
        colors = listOf(
            (if (litProgress > 0.3f) Amber400 else NeonCyan).copy(alpha = 0.75f * litProgress + 0.20f),
            (if (litProgress > 0.3f) Amber500 else Color(0xFF0369A1)).copy(alpha = 0.65f * litProgress + 0.15f),
            (if (litProgress > 0.3f) Amber300 else Color(0xFF0C4A6E)).copy(alpha = 0.50f * litProgress + 0.10f)
        ),
        startY = liquidTopY,
        endY = liquidInnerRect.bottom
    )

    val wavePath = Path().apply {
        moveTo(liquidInnerRect.left, liquidInnerRect.bottom)
        lineTo(liquidInnerRect.left, liquidTopY)
        val waveW = liquidInnerRect.width
        val step = waveW / 4f
        for (i in 0..4) {
            val wx = liquidInnerRect.left + i * step
            val wy = liquidTopY + sin((liquidPhase + i * 1.5).toDouble()).toFloat() * (2.2f * (0.4f + 0.6f * litProgress))
            if (i == 0) lineTo(wx, wy) else lineTo(wx, wy)
        }
        lineTo(liquidInnerRect.right, liquidInnerRect.bottom)
        close()
    }

    drawPath(path = wavePath, brush = liquidBrush)

    // Glass body border highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                if (litProgress > 0.5f) GlassFrostBorderLit else GlassFrostBorder,
                Color.White.copy(alpha = 0.10f),
                if (litProgress > 0.5f) GlassFrostBorderLit else GlassFrostBorder
            )
        ),
        topLeft = bodyRect.topLeft,
        size = bodyRect.size,
        cornerRadius = CornerRadius(bodyRadius, bodyRadius),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // 3. PHYSICAL INTERACTIVE SWITCH
    val switchTrackWidth = bodyWidth * 0.44f
    val switchTrackHeight = bodyHeight * 0.28f
    val switchTrackX = centerX - switchTrackWidth / 2f
    val switchTrackY = bodyTopY + bodyHeight * 0.12f
    val switchRadius = 6.dp.toPx()

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF050505),
                Color(0xFF1E293B)
            )
        ),
        topLeft = Offset(switchTrackX, switchTrackY),
        size = Size(switchTrackWidth, switchTrackHeight),
        cornerRadius = CornerRadius(switchRadius, switchRadius)
    )

    val knobHeight = switchTrackHeight * 0.48f
    val knobWidth = switchTrackWidth - 3.dp.toPx()
    val knobMaxTravel = switchTrackHeight - knobHeight - 3.dp.toPx()
    val knobY = switchTrackY + 1.5.dp.toPx() + (1f - switchProgress) * knobMaxTravel
    val knobX = switchTrackX + 1.5.dp.toPx()

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                if (litProgress > 0.5f) Amber300 else Slate400,
                if (litProgress > 0.5f) Amber500 else Color(0xFF475569)
            )
        ),
        topLeft = Offset(knobX, knobY),
        size = Size(knobWidth, knobHeight),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )

    drawCircle(
        color = if (litProgress > 0.5f) Color.White else Amber400.copy(alpha = 0.6f),
        radius = 2.5.dp.toPx(),
        center = Offset(centerX, knobY + knobHeight / 2f)
    )

    // 4. TAIL CAP
    val tailY = bodyTopY + bodyHeight
    val tailPath = Path().apply {
        moveTo(centerX - tailCapWidth / 2f, tailY)
        lineTo(centerX + tailCapWidth / 2f, tailY)
        lineTo(centerX + (tailCapWidth * 0.75f) / 2f, tailY + tailCapHeight)
        lineTo(centerX - (tailCapWidth * 0.75f) / 2f, tailY + tailCapHeight)
        close()
    }
    drawPath(
        path = tailPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF334155),
                Color(0xFF0F172A)
            )
        )
    )

    // 5. FLASH HEAD
    val headPath = Path().apply {
        moveTo(centerX - headTopWidth / 2f, baseY)
        lineTo(centerX + headTopWidth / 2f, baseY)
        lineTo(centerX + headBottomWidth / 2f, baseY + headHeight)
        lineTo(centerX - headBottomWidth / 2f, baseY + headHeight)
        close()
    }

    val headBrush = Brush.verticalGradient(
        colors = listOf(
            (if (litProgress > 0.5f) Color(0xFFFEF3C7) else Color(0xFF475569)).copy(alpha = 0.90f),
            (if (litProgress > 0.5f) Amber500 else Color(0xFF1E293B)).copy(alpha = 0.85f),
            Color(0xFF0F172A).copy(alpha = 0.95f)
        ),
        startY = baseY,
        endY = baseY + headHeight
    )
    drawPath(path = headPath, brush = headBrush)

    // 6. OPTICAL LENS
    val lensOvalRect = Rect(
        offset = Offset(centerX - headTopWidth / 2f, baseY - 5.dp.toPx()),
        size = Size(headTopWidth, 12.dp.toPx())
    )

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                if (litProgress > 0.2f) Color.White else Color(0xFF1E293B),
                if (litProgress > 0.2f) Color(0xFFFEF08A) else Color(0xFF0B1329),
                if (litProgress > 0.2f) Amber400 else Color(0xFF030712)
            ),
            center = Offset(centerX, baseY),
            radius = headTopWidth / 2f
        ),
        topLeft = lensOvalRect.topLeft,
        size = lensOvalRect.size
    )

    if (litProgress > 0.1f) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f * litProgress),
                    Amber300.copy(alpha = 0.85f * litProgress),
                    Amber400.copy(alpha = 0.55f * litProgress),
                    Color.Transparent
                ),
                center = Offset(centerX, baseY),
                radius = headTopWidth * (0.45f * litProgress)
            ),
            topLeft = lensOvalRect.topLeft,
            size = lensOvalRect.size
        )
    }

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.Transparent,
                Color.White.copy(alpha = 0.9f)
            )
        ),
        topLeft = lensOvalRect.topLeft,
        size = lensOvalRect.size,
        style = Stroke(width = 1.dp.toPx())
    )
}

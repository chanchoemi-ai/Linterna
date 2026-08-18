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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.GlassFrostBorder
import com.example.ui.theme.GlassFrostBorderLit
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Immersive Background with deep obsidian canvas (#050505) and ambient amber/luminescent aura.
 */
@Composable
fun LiquidBackground(
    isLit: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "immersive_ambient")

    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )

    val litGlowAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.50f else 0.08f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "lit_glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .drawBehind {
                val width = size.width
                val height = size.height

                // Deep Obsidian gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBlack,
                            ObsidianElevated,
                            Color(0xFF030303)
                        )
                    )
                )

                // Central Amber Ambient Aura (w-[300px] h-[300px] bg-amber-500/10 blur-[100px])
                val cx = width * 0.5f + cos(t1.toDouble()).toFloat() * (width * 0.06f)
                val cy = height * 0.45f + sin(t1.toDouble()).toFloat() * (height * 0.04f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Amber500.copy(alpha = litGlowAlpha * 0.70f),
                            Amber400.copy(alpha = litGlowAlpha * 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = width * 0.75f
                    ),
                    center = Offset(cx, cy),
                    radius = width * 0.75f
                )

                // Top ambient lens sheen
                if (isLit) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFEF3C7).copy(alpha = 0.30f),
                                Amber500.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.5f, height * 0.32f),
                            radius = width * 0.55f
                        ),
                        center = Offset(width * 0.5f, height * 0.32f),
                        radius = width * 0.55f
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * Immersive Glass Card Modifier with frosted translucent surface, specular border gradient, and soft shadow.
 */
@Composable
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    isLit: Boolean = false,
    alpha: Float = 0.06f,
    elevation: Dp = 8.dp
): Modifier {
    val borderColor = if (isLit) GlassFrostBorderLit else GlassFrostBorder
    val bgAlpha = if (isLit) alpha * 1.8f else alpha

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.7f),
            spotColor = if (isLit) Amber400.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.6f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = bgAlpha * 1.2f),
                    Color(0xFF1E293B).copy(alpha = bgAlpha * 1.5f),
                    Color(0xFF0F172A).copy(alpha = bgAlpha * 2.0f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = if (isLit) 0.6f else 0.25f),
                    Color.White.copy(alpha = 0.05f),
                    borderColor.copy(alpha = if (isLit) 0.4f else 0.12f)
                ),
                start = Offset(0f, 0f),
                end = Offset(300f, 300f)
            ),
            shape = shape
        )
}

/**
 * Immersive Navigation Pill Item with Luminous Amber Dot Indicator.
 */
@Composable
fun ImmersiveNavModeItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "mode_chip_$text"
) {
    val interactionSource = remember { MutableInteractionSource() }

    val activeAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.40f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "nav_item_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "nav_item_scale"
    )

    Column(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Amber400),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Luminous active amber dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isSelected) Amber400 else Color.Transparent)
        )

        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) Amber400 else Slate400.copy(alpha = activeAlpha),
            modifier = Modifier
                .padding(top = 4.dp, bottom = 2.dp)
                .size(22.dp)
        )

        Text(
            text = text,
            color = if (isSelected) Amber400 else Slate500.copy(alpha = activeAlpha),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Expanding Liquid Shockwave Ripple ring on Flashlight Toggle
 */
@Composable
fun LiquidRippleRings(
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isLit) return

    val infiniteTransition = rememberInfiniteTransition(label = "ripple_transition")

    val ripple1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.44f)
        val maxRadius = size.width * 0.70f

        val r1 = maxRadius * ripple1
        val a1 = (1f - ripple1) * 0.25f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Amber400.copy(alpha = 0f),
                    Amber400.copy(alpha = a1),
                    Color.Transparent
                ),
                center = center,
                radius = r1
            ),
            center = center,
            radius = r1
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicOrbVisualizer(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioRms: Float,
    onOrbTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animations")

    // Rotation angle for holographic rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Reverse rotation for counter ring
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Breathing pulse for idle state
    val breathPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_pulse"
    )

    // Waveform phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val activeScale = when {
        isSpeaking -> 1.08f + (breathPulse - 1f) * 1.5f
        isListening -> 1.0f + (audioRms * 0.25f)
        else -> breathPulse
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onOrbTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 3.4f
            val dynamicRadius = baseRadius * activeScale

            // Outer Aura Glow
            val auraColor = when {
                isSpeaking -> NeonPurpleBright.copy(alpha = 0.35f)
                isListening -> NeonCyanSecondary.copy(alpha = 0.4f)
                else -> NeonPurpleContainer.copy(alpha = 0.2f)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor, Color.Transparent),
                    center = center,
                    radius = dynamicRadius * 1.8f
                ),
                radius = dynamicRadius * 1.8f,
                center = center
            )

            // Outer Rotating Holographic Orbit Ring 1 (Tick marks)
            val outerOrbitRadius = dynamicRadius * 1.35f
            val numTicks = 36
            for (i in 0 until numTicks) {
                val tickAngle = (i * (360f / numTicks) + rotationAngle) * (PI / 180f).toFloat()
                val tickLen = if (i % 4 == 0) 12f else 6f
                val start = Offset(
                    center.x + (outerOrbitRadius - tickLen) * cos(tickAngle),
                    center.y + (outerOrbitRadius - tickLen) * sin(tickAngle)
                )
                val end = Offset(
                    center.x + outerOrbitRadius * cos(tickAngle),
                    center.y + outerOrbitRadius * sin(tickAngle)
                )
                val tickColor = if (i % 4 == 0) NeonCyanSecondary.copy(alpha = 0.8f) else NeonPurpleBright.copy(alpha = 0.35f)
                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 4 == 0) 2.5f else 1.5f
                )
            }

            // Outer Counter-Rotating Ring 2
            drawCircle(
                color = if (isListening) NeonCyanSecondary.copy(alpha = 0.6f) else NeonPurplePrimary.copy(alpha = 0.4f),
                radius = dynamicRadius * 1.18f,
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(25f, 15f), counterRotationAngle)
                )
            )

            // Dynamic Audio Waveform Sine Ribbon
            if (isSpeaking || isListening) {
                val wavePath = Path()
                val waveAmplitude = if (isSpeaking) 18f else (10f + audioRms * 25f)
                val waveSegments = 40
                val waveRadius = dynamicRadius * 1.08f

                for (step in 0..waveSegments) {
                    val angle = (step.toFloat() / waveSegments) * 2f * PI.toFloat()
                    val waveOffset = sin(angle * 6f + wavePhase) * waveAmplitude
                    val r = waveRadius + waveOffset
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (step == 0) {
                        wavePath.moveTo(x, y)
                    } else {
                        wavePath.lineTo(x, y)
                    }
                }
                wavePath.close()

                drawPath(
                    path = wavePath,
                    color = if (isSpeaking) NeonPinkTertiary.copy(alpha = 0.75f) else NeonCyanSecondary.copy(alpha = 0.85f),
                    style = Stroke(width = 3f)
                )
            }

            // 3D Spherical Core (Radial Gradient)
            val coreGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    NeonPurpleBright,
                    OrbCoreGlow,
                    FuturisticDarkSurface
                ),
                center = Offset(center.x - dynamicRadius * 0.25f, center.y - dynamicRadius * 0.3f),
                radius = dynamicRadius
            )

            drawCircle(
                brush = coreGradient,
                radius = dynamicRadius,
                center = center
            )

            // Inner Core Cyber Eye / Brain Glow
            val eyeColor = if (isSpeaking) NeonPinkTertiary else NeonCyanSecondary
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, eyeColor, Color.Transparent),
                    center = center,
                    radius = dynamicRadius * 0.45f
                ),
                radius = dynamicRadius * 0.45f,
                center = center
            )

            // Inner Robot Eye Iris ring
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = dynamicRadius * 0.18f,
                center = center,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = eyeColor,
                radius = dynamicRadius * 0.08f,
                center = center
            )
        }
    }
}

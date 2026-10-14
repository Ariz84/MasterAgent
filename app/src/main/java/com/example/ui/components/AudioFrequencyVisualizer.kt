package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyanSecondary
import com.example.ui.theme.NeonPinkTertiary
import com.example.ui.theme.NeonPurpleBright
import kotlin.math.sin

@Composable
fun AudioFrequencyVisualizer(
    isActive: Boolean,
    audioRms: Float,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val barCount = 28
        val totalSpacing = size.width * 0.15f
        val barWidth = (size.width - totalSpacing) / barCount
        val spacing = totalSpacing / (barCount - 1)
        val centerY = size.height / 2f

        val gradientColors = if (isSpeaking) {
            listOf(NeonPurpleBright, NeonPinkTertiary)
        } else {
            listOf(NeonPurpleBright, NeonCyanSecondary)
        }

        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val wave = sin(normalizedIdx * 8f + phase) * 0.5f + 0.5f
            val baseHeight = if (isActive) {
                val factor = if (isSpeaking) 0.6f + wave * 0.4f else (0.2f + audioRms * 0.8f * wave)
                (size.height * factor).coerceIn(6f, size.height)
            } else {
                8f
            }

            val x = i * (barWidth + spacing)
            val y = centerY - (baseHeight / 2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = y,
                    endY = y + baseHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, baseHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

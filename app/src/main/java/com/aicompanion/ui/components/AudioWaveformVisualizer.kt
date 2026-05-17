package com.aicompanion.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    isActive: Boolean,
    amplitude: Float = 0.5f,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE91E63)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val barCount = 20
        val barWidth = size.width / barCount
        val centerY = size.height / 2

        for (i in 0 until barCount) {
            val barPhase = phase + i * 30f
            val heightFraction = if (isActive) {
                (sin(Math.toRadians(barPhase.toDouble())) * 0.5 + 0.5).toFloat() * amplitude
            } else {
                0.1f
            }
            val barHeight = size.height * heightFraction

            drawLine(
                color = color.copy(alpha = if (isActive) 0.8f else 0.3f),
                start = Offset(i * barWidth + barWidth / 2, centerY - barHeight / 2),
                end = Offset(i * barWidth + barWidth / 2, centerY + barHeight / 2),
                strokeWidth = barWidth * 0.6f
            )
        }
    }
}

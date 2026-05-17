package com.aicompanion.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = if (isListening) {
            infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        }
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer pulse ring
        if (isListening) {
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale),
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.15f)
            ) {}
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Main button
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(72.dp),
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isListening) Color(0xFFE53935)
                else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "停止" else "语音",
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
        }
    }
}

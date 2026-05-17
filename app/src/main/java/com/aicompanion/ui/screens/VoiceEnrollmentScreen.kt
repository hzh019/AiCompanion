package com.aicompanion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicompanion.ui.viewmodel.VoiceEnrollmentViewModel
import com.aicompanion.voice.EnrollmentStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceEnrollmentScreen(
    viewModel: VoiceEnrollmentViewModel = hiltViewModel(),
    onComplete: () -> Unit = {}
) {
    val state by viewModel.enrollmentState.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("声音注册") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.reset(); onComplete() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state.step) {
                EnrollmentStep.READY -> {
                    Text(
                        "声音注册",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "为了让AI只能识别你的声音，\n请录制几段语音样本",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.startEnrollment(3) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mic, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始注册")
                    }
                }

                EnrollmentStep.RECORDING_PHRASE -> {
                    // RMS level visualization
                    val waveColor = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    ) {
                        val radius = (size.minDimension / 2) * (0.5f + rmsLevel * 2f)
                        drawCircle(
                            color = waveColor.copy(alpha = 0.3f),
                            radius = size.minDimension / 2
                        )
                        drawCircle(
                            color = waveColor,
                            radius = radius
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "请朗读以下文字",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            state.currentPhraseText,
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "进度：${state.currentPhraseIndex + 1} / ${state.totalPhrases}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.recordPhrase() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FiberManualRecord, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("点击录制")
                    }
                }

                EnrollmentStep.PROCESSING -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                EnrollmentStep.COMPLETE -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完成")
                    }
                }

                EnrollmentStep.ERROR -> {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

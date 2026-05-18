package com.aicompanion.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicompanion.conversation.ConversationState
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.ui.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
    onNavigateToPersonality: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {},
    onNavigateToVoiceEnrollment: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // System voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            viewModel.onVoiceResult(matches?.firstOrNull())
        } else {
            viewModel.onVoiceResult(null)
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(uiState.messages.size, uiState.responseText) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AI 伴侣")
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(uiState.conversationState)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToVoiceEnrollment) {
                        Icon(Icons.Default.VoiceOverOff, "声音注册")
                    }
                    IconButton(onClick = onNavigateToMemories) {
                        Icon(Icons.Default.Psychology, "记忆")
                    }
                    IconButton(onClick = onNavigateToPersonality) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mic button - launches system voice dialog
                    FilledIconButton(
                        onClick = {
                            voiceLauncher.launch(viewModel.createVoiceIntent())
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (uiState.isListening)
                                Color(0xFFE53935)
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Mic, "语音",
                            tint = if (uiState.isListening) Color.White
                            else MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { viewModel.updateTextInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp)
                    )

                    FilledIconButton(
                        onClick = { viewModel.sendTextMessage() },
                        modifier = Modifier.size(48.dp),
                        enabled = textInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, "发送")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💕", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("你的AI伴侣已就绪", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("输入文字或点击麦克风开始对话", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }

            // Streaming response
            if (uiState.responseText.isNotBlank() &&
                uiState.conversationState == ConversationState.Thinking) {
                item {
                    ChatBubble(
                        message = MessageEntity(conversationId = "", role = "assistant",
                            content = uiState.responseText),
                        isStreaming = true
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(state: ConversationState) {
    val (text, color) = when (state) {
        is ConversationState.Idle -> "待机中" to MaterialTheme.colorScheme.outline
        is ConversationState.Listening -> "聆听中..." to Color(0xFF4CAF50)
        is ConversationState.Processing -> "处理中..." to Color(0xFFFF9800)
        is ConversationState.Thinking -> "思考中..." to Color(0xFF2196F3)
        is ConversationState.Speaking -> "说话中..." to Color(0xFF9C27B0)
        is ConversationState.Error -> "错误" to MaterialTheme.colorScheme.error
    }

    Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.15f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChatBubble(message: MessageEntity, isStreaming: Boolean = false) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val shape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = shape,
            color = bubbleColor,
            onClick = {}
        ) {
            Text(
                message.content + if (isStreaming) "▊" else "",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
